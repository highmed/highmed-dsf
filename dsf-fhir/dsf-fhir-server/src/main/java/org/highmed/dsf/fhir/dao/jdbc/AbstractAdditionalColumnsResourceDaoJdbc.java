package org.highmed.dsf.fhir.dao.jdbc;

import ca.uhn.fhir.context.FhirContext;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.search.DbSearchQuery;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQueryParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public abstract class AbstractAdditionalColumnsResourceDaoJdbc<R extends Resource> extends AbstractDomainResourceDaoJdbc<R> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAdditionalColumnsResourceDaoJdbc.class);

    private final List<String> additionalColumns;

    /*
     * Using a suppliers for SearchParameters, implementations are not thread safe and because of that need to be
     * created on a request basis
     */
    @SafeVarargs
    protected AbstractAdditionalColumnsResourceDaoJdbc(DataSource dataSource, FhirContext fhirContext, Class<R> resourceType,
                                                    String resourceTable, String resourceColumn, String resourceIdColumn, List<String> additionalColumns,
                                                    Supplier<SearchQueryParameter<R>>... searchParameterFactories){
        super(dataSource, fhirContext, resourceType, resourceTable, resourceColumn, resourceIdColumn, searchParameterFactories);
        this.additionalColumns = additionalColumns;
    }

    private String getAdditionalColumnsSql() {
        return additionalColumns.stream().collect(Collectors.joining(", ", ", ", ""));
    }

    private String getAdditionalWildcardsSql() {
        return additionalColumns.stream().map(s -> "?").collect(Collectors.joining(", ", ", ", ""));
    }

    private String getAdditionalColumnsForUpdate() {
        return additionalColumns.stream().map(s -> s + " = ?" ).collect(Collectors.joining(", ", ", ", ""));
    }

    @Override
    protected R create(Connection connection, R resource, UUID uuid) throws SQLException
    {
        resource = copy(resource); // XXX defensive copy, might want to remove this call
        resource.setIdElement(new IdType(getResourceTypeName(), uuid.toString(), FIRST_VERSION_STRING));
        resource.getMeta().setVersionId(FIRST_VERSION_STRING);
        resource.getMeta().setLastUpdated(new Date());

        // db version set by default value
        String sql = "INSERT INTO " + getResourceTable() + " (" + getResourceIdColumn() + ", " + getResourceColumn() + getAdditionalColumnsSql() + ") VALUES (?, ?" + getAdditionalWildcardsSql() + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            initializeCreateStatement(statement, resource, uuid);

            logger.trace("Executing query '{}'", statement);
            statement.execute();
        }

        return resource;
    }

    protected abstract PreparedStatement initializeCreateStatement(PreparedStatement statement, R resource, UUID uuid) throws SQLException;

    @Override
    public Optional<R> readWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceDeletedException
    {
        Objects.requireNonNull(connection, "connection");
        if (uuid == null)
            return Optional.empty();

        String sql = "SELECT " + getResourceColumn() + ", deleted" + getAdditionalColumnsSql() + " FROM " + getResourceTable() + " WHERE " + getResourceIdColumn() + " = ? ORDER BY version DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setObject(1, uuidToPgObject(uuid));

            logger.trace("Executing query '{}'", statement);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    if (result.getBoolean(2))
                    {
                        logger.debug("{} with IdPart {} found, but marked as deleted", getResourceTypeName(), uuid);
                        throw new ResourceDeletedException(new IdType(getResourceTypeName(), uuid.toString()));
                    }
                    else
                    {
                        logger.debug("{} with IdPart {} found", getResourceTypeName(), uuid);
                        return assembleResourceFromReadResult(result);
                    }
                }
                else
                    return Optional.empty();
            }
        }
    }

    protected abstract Optional<R> assembleResourceFromReadResult(ResultSet result) throws SQLException;

    @Override
    public Optional<R> readVersionWithTransaction(Connection connection, UUID uuid, long version) throws SQLException
    {
        Objects.requireNonNull(connection, "connection");
        if (uuid == null || version < FIRST_VERSION)
            return Optional.empty();

        String sql = "SELECT " + getResourceColumn() + getAdditionalColumnsSql() + " FROM " + getResourceTable() + " WHERE " + getResourceIdColumn() + " = ? AND version = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setObject(1, uuidToPgObject(uuid));
            statement.setLong(2, version);

            logger.trace("Executing query '{}'", statement);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    logger.debug("{} with IdPart {} and Version {} found", getResourceTypeName(), uuid, version);
                    return assembleResourceFromReadVersionResult(result);
                }
                else
                {
                    logger.debug("{} with IdPart {} and Version {} not found", getResourceTypeName(), uuid, version);
                    return Optional.empty();
                }
            }
        }
    }

    protected abstract Optional<R> assembleResourceFromReadVersionResult(ResultSet result) throws SQLException;

    @Override
    protected R update(Connection connection, R resource, long version) throws SQLException
    {
        UUID uuid = toUuid(resource.getIdElement().getIdPart());
        if (uuid == null)
            throw new IllegalArgumentException("resource.id is not a UUID");

        resource = copy(resource);
        String versionAsString = String.valueOf(version);
        resource.setIdElement(new IdType(getResourceTypeName(), resource.getIdElement().getIdPart(), versionAsString));
        resource.getMeta().setVersionId(versionAsString);
        resource.getMeta().setLastUpdated(new Date());

        String sql = "INSERT INTO " + getResourceTable() + " (" + getResourceIdColumn() + ", version, " + getResourceColumn() + getAdditionalColumnsSql() + ") VALUES (?, ?, ?" + getAdditionalWildcardsSql() + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            initializeUpdateStatement(statement, resource, uuid, version);

            logger.trace("Executing query '{}'", statement);
            statement.execute();
        }

        return resource;
    }

    protected abstract PreparedStatement initializeUpdateStatement(PreparedStatement statement, R resource, UUID uuid, long version) throws SQLException;

    @Override
    protected R updateSameRow(Connection connection, R resource) throws SQLException
    {
        UUID uuid = toUuid(resource.getIdElement().getIdPart());
        if (uuid == null)
            throw new IllegalArgumentException("resource.id.idPart is not a UUID");
        Long version = toLong(resource.getIdElement().getVersionIdPart());
        if (version == null)
            throw new IllegalArgumentException("resource.id.versionPart is not a number >= " + FIRST_VERSION_STRING);

        resource = copy(resource);

        String sql = "UPDATE " + getResourceTable() + " SET " + getResourceColumn() + " = ?" + getAdditionalColumnsForUpdate() + " WHERE " + getResourceIdColumn() + " = ? AND version = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            initializeUpdateSameRowStatement(statement, resource, uuid, version);
            logger.trace("Executing query '{}'", statement);
            statement.execute();
        }

        return resource;
    }

    protected abstract PreparedStatement initializeUpdateSameRowStatement(PreparedStatement statement, R resource, UUID uuid, long version) throws SQLException;


    @Override
    public PartialResult<R> searchWithTransaction(Connection connection, DbSearchQuery query) throws SQLException {
        PartialResult<R> resultWithoutAdditionalColumns = super.searchWithTransaction(connection, query);
        return getSearchAdditionalColumns(connection, resultWithoutAdditionalColumns);

    }

    private PartialResult<R> getSearchAdditionalColumns(Connection connection, PartialResult partialResult) throws SQLException {
        List<R> resources = partialResult.getPartialResult();
        List<DomainResource> includes = partialResult.getIncludes();

        for(R resource : resources) {
            completeResource(connection, resource);
        }

        for(DomainResource resource : includes) {
            if(getResourceType().isInstance(resource)) {
                completeResource(connection, ((R) resource));
            }
        }

        return new PartialResult<>(partialResult.getOverallCount(), partialResult.getPageAndCount(), resources, includes, partialResult.isCountOnly());
    }

    private void completeResource(Connection connection, R resource) throws SQLException {
        UUID uuid = toUuid(resource.getIdElement().getIdPart());
        if (uuid == null)
            throw new IllegalArgumentException("resource.id.idPart is not a UUID");
        Long version = toLong(resource.getMeta().getVersionId());
        if (version == null)
            throw new IllegalArgumentException(resource.getMeta().getVersionId() + " resource.meta.versionId is not a number >= " + FIRST_VERSION_STRING);

        String sql = "SELECT " + getAdditionalColumnsSql().substring(1) + " FROM " + getResourceTable() + " WHERE " + getResourceIdColumn() + " = ? AND version = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            initializeReadAdditionalColumnsStatement(statement, uuid, version);
            try(ResultSet result = statement.executeQuery()) {
                if(result.next()) {
                    assembleResourceFromReadAdditionalColumnsResult(result, resource);
                }
            }
            logger.trace("Executing query '{}'", statement);
            statement.execute();
        }
    }

    protected abstract PreparedStatement initializeReadAdditionalColumnsStatement(PreparedStatement statement, UUID uuid, long version) throws SQLException;

    protected abstract R assembleResourceFromReadAdditionalColumnsResult(ResultSet result, R resource) throws SQLException;
}
