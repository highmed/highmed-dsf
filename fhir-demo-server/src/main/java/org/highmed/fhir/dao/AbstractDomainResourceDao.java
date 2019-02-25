package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.search.DbSearchQuery;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQueryParameter;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.SearchQuery.SearchQueryBuilder;
import org.highmed.fhir.search.parameters.ResourceId;
import org.highmed.fhir.search.parameters.ResourceLastUpdated;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public abstract class AbstractDomainResourceDao<R extends DomainResource> implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractDomainResourceDao.class);

	private final BasicDataSource dataSource;
	private final FhirContext fhirContext;
	private final Class<R> resourceType;

	private final String resourceTable;
	private final String resourceColumn;
	private final String resourceIdColumn;

	private final List<Supplier<SearchQueryParameter<R>>> searchParameterFactories;

	private final String resourceTypeName;

	/*
	 * Using a suppliers for SearchParameters, because implementations are not thread safe and need to be created on a
	 * request basis
	 */
	@SafeVarargs
	public AbstractDomainResourceDao(BasicDataSource dataSource, FhirContext fhirContext, Class<R> resourceType,
			String resourceTable, String resourceColumn, String resourceIdColumn,
			Supplier<SearchQueryParameter<R>>... searchParameterFactories)
	{
		this.dataSource = dataSource;
		this.fhirContext = fhirContext;
		this.resourceType = resourceType;

		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;
		this.resourceIdColumn = resourceIdColumn;
		this.searchParameterFactories = Arrays.asList(searchParameterFactories);

		resourceTypeName = Objects.requireNonNull(resourceType, "resourceType").getAnnotation(ResourceDef.class).name();
	}

	protected IParser getJsonParser()
	{
		IParser p = Objects.requireNonNull(fhirContext, "fhirContext").newJsonParser();
		p.setStripVersionsFromReferences(false);
		return p;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(resourceTable, "resourceTable");
		Objects.requireNonNull(resourceColumn, "resourceColumn");
		Objects.requireNonNull(resourceIdColumn, "resourceIdColumn");
	}

	protected BasicDataSource getDataSource()
	{
		return dataSource;
	}

	protected String getResourceTable()
	{
		return resourceTable;
	}

	protected String getResourceIdColumn()
	{
		return resourceIdColumn;
	}

	protected String getResourceColumn()
	{
		return resourceColumn;
	}

	protected String getResourceTypeName()
	{
		return resourceTypeName;
	}

	public Class<R> getResourceType()
	{
		return resourceType;
	}

	public final R create(R resource) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			R inserted = create(connection, resource, UUID.randomUUID());

			logger.debug("{} with ID {} created", resourceTypeName, inserted.getId());
			return inserted;
		}
	}

	private R create(Connection connection, R resource, UUID uuid) throws SQLException
	{
		resource = copy(resource); // XXX defensive copy, might want to remove this call
		resource.setIdElement(new IdType(resourceTypeName, uuid.toString(), "1"));
		resource.getMeta().setVersionId("1");
		resource.getMeta().setLastUpdated(new Date());

		// db version set by default value
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO " + resourceTable + " (" + resourceIdColumn + ", " + resourceColumn + ") VALUES (?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(uuid));
			statement.setObject(2, resourceToPgObject(resource));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	protected abstract R copy(R resource);

	protected final Object resourceToPgObject(R resource)
	{
		if (resource == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("JSONB");
			o.setValue(getJsonParser().encodeResourceToString(resource));
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected final PGobject uuidToPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected R getResource(ResultSet result, int index) throws SQLException
	{
		String json = result.getString(index);
		return getJsonParser().parseResource(resourceType, json);
	}

	public final boolean hasNonDeletedResource(UUID uuid) throws SQLException
	{
		if (uuid == null)
			return false;

		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM " + resourceTable
						+ " WHERE " + resourceIdColumn + " = ? AND NOT deleted"))
		{
			statement.setObject(1, uuidToPgObject(uuid));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				return result.next() && result.getInt(1) > 0;
			}
		}
	}

	public final Optional<R> read(UUID uuid) throws SQLException, ResourceDeletedException
	{
		if (uuid == null)
			return Optional.empty();

		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + ", deleted FROM "
						+ resourceTable + " WHERE " + resourceIdColumn + " = ? ORDER BY version LIMIT 1"))
		{
			statement.setObject(1, uuidToPgObject(uuid));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					if (result.getBoolean(2))
					{
						logger.debug("{} with IdPart {} found, but marked as deleted", resourceTypeName, uuid);
						throw new ResourceDeletedException(new IdType(resourceTypeName, uuid.toString()));
					}
					else
					{
						logger.debug("{} with IdPart {} found", resourceTypeName, uuid);
						return Optional.of(getResource(result, 1));
					}
				}
				else
					return Optional.empty();
			}
		}
	}

	public final Optional<R> readVersion(UUID uuid, long version) throws SQLException
	{
		if (uuid == null)
			return Optional.empty();

		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + " FROM "
						+ resourceTable + " WHERE " + resourceIdColumn + " = ? AND version = ?"))
		{
			statement.setObject(1, uuidToPgObject(uuid));
			statement.setLong(2, version);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					logger.debug("{} with IdPart {} and Version {} found", resourceTypeName, uuid, version);
					return Optional.of(getResource(result, 1));
				}
				else
				{
					logger.debug("{} with IdPart {} and Version {} not found", resourceTypeName, uuid, version);
					return Optional.empty();
				}
			}
		}
	}

	public final R update(R resource) throws SQLException, ResourceNotFoundException
	{
		resource = copy(resource); // XXX defensive copy, might want to remove this call

		Objects.requireNonNull(resource, "resource");

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			connection.setAutoCommit(false);

			try
			{
				LatestVersion latestVersion = getLatestVersion(resource, connection);
				long newVersion = latestVersion.version + 1;

				R updated = update(connection, resource, newVersion);
				if (latestVersion.deleted)
					markDeleted(connection, toUuid(updated.getIdElement().getIdPart()), false);

				connection.commit();

				logger.debug("{} with IdPart {} updated, new version {}", resourceTypeName,
						updated.getIdElement().getIdPart(), newVersion);
				return updated;
			}
			catch (Exception e)
			{
				connection.rollback();
				throw e;
			}
		}
	}

	protected final UUID toUuid(String idPart)
	{
		if (idPart == null)
			return null;

		// TODO control flow by exception
		try
		{
			return UUID.fromString(idPart);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private R update(Connection connection, R resource, long newVersion) throws SQLException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new IllegalArgumentException("resource.id is not a UUID");

		String newVersionAsString = String.valueOf(newVersion);
		IdType newId = new IdType(resourceTypeName, resource.getIdElement().getIdPart(), newVersionAsString);

		resource = copy(resource);
		resource.setIdElement(newId);
		resource.getMeta().setVersionId(newVersionAsString);
		resource.getMeta().setLastUpdated(new Date());

		try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + resourceTable + " ("
				+ resourceIdColumn + ", version, " + resourceColumn + ") VALUES (?, ?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(uuid));
			statement.setLong(2, newVersion);
			statement.setObject(3, resourceToPgObject(resource));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	protected static class LatestVersion
	{
		final long version;
		final boolean deleted;

		LatestVersion(long version, boolean deleted)
		{
			this.version = version;
			this.deleted = deleted;
		}
	}

	protected final LatestVersion getLatestVersion(R resource, Connection connection)
			throws SQLException, ResourceNotFoundException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new ResourceNotFoundException(resource.getId());

		try (PreparedStatement statement = connection.prepareStatement("SELECT version, deleted FROM " + resourceTable
				+ " WHERE " + resourceIdColumn + " = ? ORDER BY version LIMIT 1"))
		{
			statement.setObject(1, uuidToPgObject(uuid));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					long version = result.getLong(1);
					boolean deleted = result.getBoolean(2);

					logger.debug(
							"Latest version for {} with IdPart {} is {}"
									+ (deleted ? ", resource marked as deleted" : ""),
							resourceTypeName, resource.getIdElement().getIdPart(), version);
					return new LatestVersion(version, deleted);
				}
				else
				{
					logger.debug("{} with IdPart {} not found", resourceTypeName, resource.getIdElement().getIdPart());
					throw new ResourceNotFoundException(resource.getId());
				}
			}
		}
	}

	public final void delete(UUID uuid) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);
			markDeleted(connection, uuid, true);
		}
	}

	protected final void markDeleted(Connection connection, UUID uuid, boolean deleted) throws SQLException
	{
		if (uuid == null)
			return;

		try (PreparedStatement statement = connection
				.prepareStatement("UPDATE " + resourceTable + " SET deleted = ? WHERE " + resourceIdColumn + " = ?"))
		{
			statement.setBoolean(1, deleted);
			statement.setObject(2, uuidToPgObject(uuid));

			logger.trace("Executing query '{}'", statement);
			statement.execute();

			logger.debug("{} with ID {} marked as deleted", resourceTypeName, uuid);
		}
	}

	public final PartialResult<R> search(DbSearchQuery query) throws SQLException
	{
		try (Connection connection = getDataSource().getConnection())
		{
			int overallCount = 0;
			try (PreparedStatement statement = connection.prepareStatement(query.getCountSql()))
			{
				query.modifyStatement(statement);

				logger.trace("Executing query '{}'", statement);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
						overallCount = result.getInt(1);
				}
			}

			List<R> partialResult = new ArrayList<>();

			if (!query.isCountOnly(overallCount)) // TODO ask db if count 0
			{
				try (PreparedStatement statement = connection.prepareStatement(query.getSearchSql()))
				{
					query.modifyStatement(statement);

					logger.trace("Executing query '{}'", statement);
					try (ResultSet result = statement.executeQuery())
					{
						while (result.next())
							partialResult.add(getResource(result, 1));

					}
				}
			}

			return new PartialResult<>(overallCount, query.getPageAndCount(), partialResult,
					query.isCountOnly(overallCount));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final SearchQuery<R> createSearchQuery(int effectivePage, int effectiveCount)
	{
		return SearchQueryBuilder
				.create(getResourceType(), getResourceTable(), getResourceIdColumn(), getResourceColumn(),
						effectivePage, effectiveCount)
				.with(new ResourceId(getResourceIdColumn()), new ResourceLastUpdated(getResourceColumn()))
				.with(searchParameterFactories.stream().map(Supplier::get).toArray(SearchQueryParameter[]::new)).build();
	}

	public final SearchQuery<R> createSearchQuery(int effectivePage, int effectiveCount, String sortParameters,
			@SuppressWarnings("unchecked") SearchQueryParameter<R>... parameters)
	{
		return SearchQueryBuilder.create(getResourceType(), getResourceTable(), getResourceIdColumn(),
				getResourceColumn(), effectivePage, effectiveCount).with(parameters).sort(sortParameters).build();
	}
}
