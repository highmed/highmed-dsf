package org.highmed.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.fhir.search.DbSearchQuery;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.SearchQuery.SearchQueryBuilder;
import org.highmed.fhir.search.SearchQueryParameter;
import org.highmed.fhir.search.parameters.ResourceId;
import org.highmed.fhir.search.parameters.ResourceLastUpdated;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

abstract class AbstractDomainResourceDaoJdbc<R extends DomainResource> implements DomainResourceDao<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractDomainResourceDaoJdbc.class);

	private static final class DomainResourceDistinctById
	{
		private final IdType id;
		private final DomainResource resource;

		public DomainResourceDistinctById(IdType id, DomainResource resource)
		{
			this.id = id;
			this.resource = resource;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DomainResourceDistinctById other = (DomainResourceDistinctById) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}

		public DomainResource getResource()
		{
			return resource;
		}
	}

	private final DataSource dataSource;
	private final FhirContext fhirContext;
	private final Class<R> resourceType;
	private final String resourceTypeName;

	private final String resourceTable;
	private final String resourceColumn;
	private final String resourceIdColumn;

	private final List<Supplier<SearchQueryParameter<R>>> searchParameterFactories;

	/*
	 * Using a suppliers for SearchParameters, because implementations are not thread safe and need to be created on a
	 * request basis
	 */
	@SafeVarargs
	public AbstractDomainResourceDaoJdbc(DataSource dataSource, FhirContext fhirContext, Class<R> resourceType,
			String resourceTable, String resourceColumn, String resourceIdColumn,
			Supplier<SearchQueryParameter<R>>... searchParameterFactories)
	{
		this.dataSource = dataSource;
		this.fhirContext = fhirContext;
		this.resourceType = resourceType;
		resourceTypeName = Objects.requireNonNull(resourceType, "resourceType").getAnnotation(ResourceDef.class).name();

		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;
		this.resourceIdColumn = resourceIdColumn;
		this.searchParameterFactories = Arrays.asList(searchParameterFactories);
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

	protected DataSource getDataSource()
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

	@Override
	public String getResourceTypeName()
	{
		return resourceTypeName;
	}

	@Override
	public final R create(R resource) throws SQLException
	{
		Objects.requireNonNull(resource, "resource");

		return createWithId(resource, UUID.randomUUID());
	}

	@Override
	public R createWithId(R resource, UUID uuid) throws SQLException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(uuid, "uuid");

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);
			return createWithTransactionAndId(connection, resource, uuid);
		}
	}

	@Override
	public R createWithTransactionAndId(Connection connection, R resource, UUID uuid) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(uuid, "uuid");
		if (connection.isReadOnly())
			throw new IllegalStateException("Connection is read-only");

		R inserted = create(connection, resource, uuid);

		logger.debug("{} with ID {} created", resourceTypeName, inserted.getId());
		return inserted;
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

	protected List<DomainResource> getResources(ResultSet result, int index) throws SQLException
	{
		String json = result.getString(index);

		if (json == null)
			return Collections.emptyList();

		JsonArray array = (JsonArray) new JsonParser().parse(json);

		List<DomainResource> includes = new ArrayList<>();
		Iterator<JsonElement> it = array.iterator();
		while (it.hasNext())
		{
			JsonElement jsonElement = it.next();
			IBaseResource resource = getJsonParser().parseResource(jsonElement.toString());
			if (resource instanceof DomainResource)
				includes.add((DomainResource) resource);
			else
				logger.warn("parsed resouce of type {} not instance of {}", resource.getClass().getName(),
						DomainResource.class.getName());
		}
		return includes;
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

	@Override
	public final Optional<R> read(UUID uuid) throws SQLException, ResourceDeletedException
	{
		if (uuid == null)
			return Optional.empty();

		try (Connection connection = dataSource.getConnection())
		{
			return readWithTransaction(connection, uuid);
		}
	}

	@Override
	public Optional<R> readWithTransaction(Connection connection, UUID uuid)
			throws SQLException, ResourceDeletedException
	{
		Objects.requireNonNull(connection, "connection");
		if (uuid == null)
			return Optional.empty();

		try (PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + ", deleted FROM "
				+ resourceTable + " WHERE " + resourceIdColumn + " = ? ORDER BY version DESC LIMIT 1"))
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

	@Override
	public final Optional<R> readVersion(UUID uuid, long version) throws SQLException
	{
		if (uuid == null)
			return Optional.empty();

		try (Connection connection = dataSource.getConnection())
		{
			return readVersionWithTransaction(connection, uuid, version);
		}
	}

	@Override
	public Optional<R> readVersionWithTransaction(Connection connection, UUID uuid, long version) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		if (uuid == null)
			return Optional.empty();

		try (PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + " FROM "
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

	@Override
	public final R update(R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		// Objects.requireNonNull(resource, "resource");
		// // expectedVersion may be null
		//
		// resource = copy(resource); // XXX defensive copy, might want to remove this call
		//
		// Objects.requireNonNull(resource, "resource");
		//
		// try (Connection connection = dataSource.getConnection())
		// {
		// connection.setReadOnly(false);
		// connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		// connection.setAutoCommit(false);
		//
		// try
		// {
		// LatestVersion latestVersion = getLatestVersion(resource, connection);
		//
		// if (expectedVersion != null && expectedVersion != latestVersion.version)
		// {
		// logger.info("Expected version {} does not match latest version {}", expectedVersion,
		// latestVersion.version);
		// throw new ResourceVersionNoMatchException(resource.getIdElement().getIdPart(), expectedVersion,
		// latestVersion.version);
		// }
		//
		// long newVersion = latestVersion.version + 1;
		//
		// R updated = update(connection, resource, newVersion);
		// if (latestVersion.deleted) // TODO check if resurrection need undelete for old versions
		// markDeleted(connection, toUuid(updated.getIdElement().getIdPart()), false);
		//
		// connection.commit();
		//
		// logger.debug("{} with IdPart {} updated, new version {}", resourceTypeName,
		// updated.getIdElement().getIdPart(), newVersion);
		// return updated;
		// }
		// catch (Exception e)
		// {
		// connection.rollback();
		// throw e;
		// }
		// }

		Objects.requireNonNull(resource, "resource");
		// expectedVersion may be null

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			connection.setAutoCommit(false);

			try
			{
				R updatedResource = updateWithTransaction(connection, resource, expectedVersion);

				connection.commit();

				return updatedResource;
			}
			catch (Exception e)
			{
				connection.rollback();
				throw e;
			}
		}
	}

	@Override
	public R updateWithTransaction(Connection connection, R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(resource, "resource");
		// expectedVersion may be null
		if (connection.isReadOnly())
			throw new IllegalStateException("Connection is read-only");
		if (connection.getTransactionIsolation() != Connection.TRANSACTION_REPEATABLE_READ
				&& connection.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE)
			throw new IllegalStateException("Connection transaction isolation not REPEATABLE_READ or SERIALIZABLE");
		if (connection.getAutoCommit())
			throw new IllegalStateException("Connection transaction is in auto commit mode");

		resource = copy(resource); // XXX defensive copy, might want to remove this call

		LatestVersion latestVersion = getLatestVersion(resource, connection);

		if (expectedVersion != null && expectedVersion != latestVersion.version)
		{
			logger.info("Expected version {} does not match latest version {}", expectedVersion, latestVersion.version);
			throw new ResourceVersionNoMatchException(resource.getIdElement().getIdPart(), expectedVersion,
					latestVersion.version);
		}

		long newVersion = latestVersion.version + 1;

		R updated = update(connection, resource, newVersion);
		if (latestVersion.deleted) // TODO check if resurrection needs undelete for old versions
			markDeleted(connection, toUuid(updated.getIdElement().getIdPart()), false);

		logger.debug("{} with IdPart {} updated, new version {}", resourceTypeName, updated.getIdElement().getIdPart(),
				newVersion);
		return updated;
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
				+ " WHERE " + resourceIdColumn + " = ? ORDER BY version DESC LIMIT 1"))
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

	@Override
	public final void delete(UUID uuid) throws SQLException
	{
		if (uuid == null)
			return;

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			deleteWithTransaction(connection, uuid);
		}
	}

	@Override
	public void deleteWithTransaction(Connection connection, UUID uuid) throws SQLException
	{
		if (uuid == null)
			return;
		Objects.requireNonNull(connection, "connection");
		if (connection.isReadOnly())
			throw new IllegalStateException("Connection is read-only");

		markDeleted(connection, uuid, true);
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

	@Override
	public final PartialResult<R> search(DbSearchQuery query) throws SQLException
	{
		Objects.requireNonNull(query, "query");

		try (Connection connection = getDataSource().getConnection())
		{
			return searchWithTransaction(connection, query);
		}
	}

	@Override
	public PartialResult<R> searchWithTransaction(Connection connection, DbSearchQuery query) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(query, "query");

		int overallCount = 0;
		try (PreparedStatement statement = connection.prepareStatement(query.getCountSql()))
		{
			query.modifyStatement(statement, connection::createArrayOf);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					overallCount = result.getInt(1);
			}
		}

		List<R> partialResult = new ArrayList<>();
		List<DomainResource> includes = new ArrayList<>();

		if (!query.isCountOnly(overallCount)) // TODO ask db if count 0
		{
			try (PreparedStatement statement = connection.prepareStatement(query.getSearchSql()))
			{
				query.modifyStatement(statement, connection::createArrayOf);

				logger.trace("Executing query '{}'", statement);
				try (ResultSet result = statement.executeQuery())
				{
					ResultSetMetaData metaData = result.getMetaData();
					while (result.next())
					{
						partialResult.add(getResource(result, 1));

						for (int c = 2; c <= metaData.getColumnCount(); c++)
							includes.addAll(getResources(result, c));
					}
				}
			}
		}

		includes = includes.stream().map(r -> new DomainResourceDistinctById(r.getIdElement(), r)).distinct()
				.map(DomainResourceDistinctById::getResource).collect(Collectors.toList());

		return new PartialResult<>(overallCount, query.getPageAndCount(), partialResult, includes,
				query.isCountOnly(overallCount));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final SearchQuery<R> createSearchQuery(int effectivePage, int effectiveCount)
	{
		return SearchQueryBuilder
				.create(resourceType, getResourceTable(), getResourceColumn(), effectivePage, effectiveCount)
				.with(new ResourceId(getResourceIdColumn()), new ResourceLastUpdated(getResourceColumn()))
				.with(searchParameterFactories.stream().map(Supplier::get).toArray(SearchQueryParameter[]::new))
				.build();
	}
}
