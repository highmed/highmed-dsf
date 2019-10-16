package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.dsf.fhir.search.DbSearchQuery;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQuery.SearchQueryBuilder;
import org.highmed.dsf.fhir.search.SearchQueryParameter;
import org.highmed.dsf.fhir.search.parameters.ResourceId;
import org.highmed.dsf.fhir.search.parameters.ResourceLastUpdated;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
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

abstract class AbstractResourceDaoJdbc<R extends Resource> implements ResourceDao<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceDaoJdbc.class);

	private static final class ResourceDistinctById
	{
		private final IdType id;
		private final Resource resource;

		public ResourceDistinctById(IdType id, Resource resource)
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
			ResourceDistinctById other = (ResourceDistinctById) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}

		public Resource getResource()
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

	private final PreparedStatementFactory<R> preparedStatementFactory;

	/*
	 * Using a suppliers for SearchParameters, implementations are not thread safe and because of that they need to be
	 * created on a request basis
	 */
	@SafeVarargs
	AbstractResourceDaoJdbc(DataSource dataSource, FhirContext fhirContext, Class<R> resourceType, String resourceTable,
			String resourceColumn, String resourceIdColumn,
			Supplier<SearchQueryParameter<R>>... searchParameterFactories)
	{
		this(dataSource, fhirContext, resourceType, resourceTable, resourceColumn, resourceIdColumn,
				new PreparedStatementFactoryDefault<>(fhirContext, resourceType, resourceTable, resourceIdColumn,
						resourceColumn),
				searchParameterFactories);
	}

	/*
	 * Using a suppliers for SearchParameters, implementations are not thread safe and because of that they need to be
	 * created on a request basis
	 */
	@SafeVarargs
	AbstractResourceDaoJdbc(DataSource dataSource, FhirContext fhirContext, Class<R> resourceType, String resourceTable,
			String resourceColumn, String resourceIdColumn, PreparedStatementFactory<R> preparedStatementFactory,
			Supplier<SearchQueryParameter<R>>... searchParameterFactories)
	{
		this.dataSource = dataSource;
		this.fhirContext = fhirContext;
		this.resourceType = resourceType;
		resourceTypeName = Objects.requireNonNull(resourceType, "resourceType").getAnnotation(ResourceDef.class).name();

		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;
		this.resourceIdColumn = resourceIdColumn;

		this.preparedStatementFactory = preparedStatementFactory;

		this.searchParameterFactories = Arrays.asList(searchParameterFactories);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(resourceTable, "resourceTable");
		Objects.requireNonNull(resourceColumn, "resourceColumn");
		Objects.requireNonNull(resourceIdColumn, "resourceIdColumn");
		Objects.requireNonNull(preparedStatementFactory, "preparedStatementFactory");
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
	public Class<R> getResourceType()
	{
		return resourceType;
	}

	@Override
	public Connection getNewTransaction() throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setReadOnly(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		connection.setAutoCommit(false);

		return connection;
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
			throw new IllegalArgumentException("Connection is read-only");

		R inserted = create(connection, resource, uuid);

		logger.debug("{} with ID {} created", resourceTypeName, inserted.getId());
		return inserted;
	}

	private R create(Connection connection, R resource, UUID uuid) throws SQLException
	{
		resource = copy(resource); // XXX defensive copy, might want to remove this call
		resource.setIdElement(new IdType(resourceTypeName, uuid.toString(), FIRST_VERSION_STRING));
		resource.getMeta().setVersionId(FIRST_VERSION_STRING);
		resource.getMeta().setLastUpdated(new Date());

		try (PreparedStatement statement = connection.prepareStatement(preparedStatementFactory.getCreateSql()))
		{
			preparedStatementFactory.configureCreateStatement(statement, resource, uuid);

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

	private IParser getJsonParser()
	{
		IParser p = Objects.requireNonNull(fhirContext, "fhirContext").newJsonParser();
		p.setStripVersionsFromReferences(false);
		return p;
	}

	protected R getResource(ResultSet result, int index) throws SQLException
	{
		String json = result.getString(index);
		return getJsonParser().parseResource(resourceType, json);
	}

	/* caution: only works because we set all versions as deleted or not deleted in method markDeleted */
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

		try (PreparedStatement statement = connection.prepareStatement(preparedStatementFactory.getReadByIdSql()))
		{
			preparedStatementFactory.configureReadByIdStatement(statement, uuid);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					if (preparedStatementFactory.isReadByIdDeleted(result))
					{
						logger.debug("{} with IdPart {} found, but marked as deleted", resourceTypeName, uuid);
						throw new ResourceDeletedException(new IdType(resourceTypeName, uuid.toString()));
					}
					else
					{
						logger.debug("{} with IdPart {} found", resourceTypeName, uuid);
						return Optional.of(preparedStatementFactory.getReadByIdResource(result));
					}
				}
				else
				{
					logger.debug("{} with IdPart {} not found", resourceTypeName, uuid);
					return Optional.empty();
				}
			}
		}
	}

	@Override
	public final Optional<R> readVersion(UUID uuid, long version) throws SQLException
	{
		if (uuid == null || version < FIRST_VERSION)
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
		if (uuid == null || version < FIRST_VERSION)
			return Optional.empty();

		try (PreparedStatement statement = connection
				.prepareStatement(preparedStatementFactory.getReadByIdAndVersionSql()))
		{
			preparedStatementFactory.configureReadByIdAndVersionStatement(statement, uuid, version);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					logger.debug("{} with IdPart {} and Version {} found", resourceTypeName, uuid, version);
					return Optional.of(preparedStatementFactory.getReadByIdAndVersionResource(result));
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
	public boolean existsNotDeleted(String idString, String versionString) throws SQLException
	{
		return existsNotDeletedWithTransaction(dataSource.getConnection(), idString, versionString);
	}

	@Override
	public boolean existsNotDeletedWithTransaction(Connection connection, String idString, String versionString)
			throws SQLException
	{
		Objects.requireNonNull(connection, "connection");

		UUID uuid = toUuid(idString);

		if (uuid == null)
			return false;

		if (versionString == null || versionString.isBlank())
			return hasNonDeletedResource(uuid);
		else
		{
			Long version = toLong(versionString);
			if (version == null || version < FIRST_VERSION)
				return false;

			try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM " + resourceTable
					+ " WHERE " + resourceIdColumn + " = ? AND version = ? AND NOT deleted"))
			{
				statement.setObject(1, uuidToPgObject(uuid));
				statement.setLong(2, version);

				logger.trace("Executing query '{}'", statement);
				try (ResultSet result = statement.executeQuery())
				{
					return result.next() && result.getInt(1) > 0;
				}
			}
		}
	}

	@Override
	public final R update(R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException
	{
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
			throw new IllegalArgumentException("Connection is read-only");
		if (connection.getTransactionIsolation() != Connection.TRANSACTION_REPEATABLE_READ
				&& connection.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE)
			throw new IllegalArgumentException("Connection transaction isolation not REPEATABLE_READ or SERIALIZABLE");
		if (connection.getAutoCommit())
			throw new IllegalArgumentException("Connection transaction is in auto commit mode");

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

	@Override
	public R updateSameRowWithTransaction(Connection connection, R resource)
			throws SQLException, ResourceNotFoundException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(resource, "resource");
		if (!resource.hasIdElement())
			throw new IllegalArgumentException(resourceTypeName + " has no id element");
		if (resource.hasIdElement() && !resource.getIdElement().hasIdPart())
			throw new IllegalArgumentException(resourceTypeName + ".id has not id part");
		if (resource.hasIdElement() && !resource.getIdElement().hasVersionIdPart())
			throw new IllegalArgumentException(resourceTypeName + ".id has not version part");
		if (connection.isReadOnly())
			throw new IllegalArgumentException("Connection is read-only");
		if (connection.getTransactionIsolation() != Connection.TRANSACTION_REPEATABLE_READ
				&& connection.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE)
			throw new IllegalArgumentException("Connection transaction isolation not REPEATABLE_READ or SERIALIZABLE");
		if (connection.getAutoCommit())
			throw new IllegalArgumentException("Connection transaction is in auto commit mode");

		R updated = updateSameRow(connection, resource);

		logger.debug("{} with IdPart {} updated, version {} unchanged", resourceTypeName,
				updated.getIdElement().getIdPart(), resource.getIdElement().getVersionIdPart());
		return updated;
	}

	protected final UUID toUuid(String id)
	{
		if (id == null)
			return null;

		// TODO control flow by exception
		try
		{
			return UUID.fromString(id);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	protected final Long toLong(String version)
	{
		if (version == null)
			return null;

		// TODO control flow by exception
		try
		{
			return Long.parseLong(version);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	private R update(Connection connection, R resource, long version) throws SQLException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new IllegalArgumentException("resource.id is not a UUID");

		resource = copy(resource);
		String versionAsString = String.valueOf(version);
		resource.setIdElement(new IdType(resourceTypeName, resource.getIdElement().getIdPart(), versionAsString));
		resource.getMeta().setVersionId(versionAsString);
		resource.getMeta().setLastUpdated(new Date());

		try (PreparedStatement statement = connection.prepareStatement(preparedStatementFactory.getUpdateNewRowSql()))
		{
			preparedStatementFactory.configureUpdateNewRowSqlStatement(statement, uuid, version, resource);

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	private R updateSameRow(Connection connection, R resource) throws SQLException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new IllegalArgumentException("resource.id.idPart is not a UUID");
		Long version = toLong(resource.getIdElement().getVersionIdPart());
		if (version == null)
			throw new IllegalArgumentException("resource.id.versionPart is not a number >= " + FIRST_VERSION_STRING);

		resource = copy(resource);

		try (PreparedStatement statement = connection.prepareStatement(preparedStatementFactory.getUpdateSameRowSql()))
		{
			preparedStatementFactory.configureUpdateSameRowSqlStatement(statement, uuid, version, resource);

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
			throw new ResourceNotFoundException(resource.getId() != null ? resource.getId() : "'null'");

		return getLatestVersion(uuid, connection);
	}

	protected final Optional<LatestVersion> getLatestVersionIfExists(R resource, Connection connection)
			throws SQLException, ResourceNotFoundException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new ResourceNotFoundException(resource.getId() != null ? resource.getId() : "'null'");

		return getLatestVersionIfExists(uuid, connection);
	}

	protected final LatestVersion getLatestVersion(UUID uuid, Connection connection)
			throws SQLException, ResourceNotFoundException
	{
		if (uuid == null)
			throw new ResourceNotFoundException("'null'");

		return getLatestVersionIfExists(uuid, connection)
				.orElseThrow(() -> new ResourceNotFoundException(uuid.toString()));
	}

	protected final Optional<LatestVersion> getLatestVersionIfExists(UUID uuid, Connection connection)
			throws SQLException, ResourceNotFoundException
	{
		if (uuid == null)
			return Optional.empty();

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

					logger.debug("Latest version for {} with IdPart {} is {}{}", resourceTypeName, uuid.toString(),
							version, deleted ? ", resource marked as deleted" : "");
					return Optional.of(new LatestVersion(version, deleted));
				}
				else
				{
					logger.debug("{} with IdPart {} not found", resourceTypeName, uuid.toString());
					return Optional.empty();
				}
			}
		}
	}

	@Override
	public final boolean delete(UUID uuid) throws SQLException, ResourceNotFoundException
	{
		if (uuid == null)
			throw new ResourceNotFoundException("'null'");

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			return deleteWithTransaction(connection, uuid);
		}
	}

	@Override
	public boolean deleteWithTransaction(Connection connection, UUID uuid)
			throws SQLException, ResourceNotFoundException
	{
		if (uuid == null)
			throw new ResourceNotFoundException("'null'");
		Objects.requireNonNull(connection, "connection");
		if (connection.isReadOnly())
			throw new IllegalStateException("Connection is read-only");

		return markDeleted(connection, uuid, true);
	}

	/*
	 * caution: implementation of method hasNonDeletedResource only works because we set all versions as deleted or not
	 * deleted here
	 */
	protected final boolean markDeleted(Connection connection, UUID uuid, boolean deleted)
			throws SQLException, ResourceNotFoundException
	{
		if (uuid == null)
			throw new ResourceNotFoundException("'null'");

		LatestVersion latestVersion = getLatestVersion(uuid, connection);

		if (latestVersion.deleted)
			return false;

		try (PreparedStatement statement = connection
				.prepareStatement("UPDATE " + resourceTable + " SET deleted = ? WHERE " + resourceIdColumn + " = ?"))
		{
			statement.setBoolean(1, deleted);
			statement.setObject(2, uuidToPgObject(uuid));

			logger.trace("Executing query '{}'", statement);
			statement.execute();

			logger.debug("{} with ID {} marked as deleted", resourceTypeName, uuid);
			return true;
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
		List<Resource> includes = new ArrayList<>();

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
						R resource = getResource(result, 1);
						modifySearchResultResource(resource, connection);
						partialResult.add(resource);

						for (int columnIndex = 2; columnIndex <= metaData.getColumnCount(); columnIndex++)
							getResources(result, columnIndex, includes, connection, query);
					}
				}
			}
		}

		includes = includes.stream().map(r -> new ResourceDistinctById(r.getIdElement(), r)).distinct()
				.map(ResourceDistinctById::getResource).collect(Collectors.toList());

		return new PartialResult<>(overallCount, query.getPageAndCount(), partialResult, includes,
				query.isCountOnly(overallCount));
	}

	/**
	 * Override this method to modify resources retrieved by search queries before returning to the user. This method
	 * can be used, if the resource returned by the search is not complete and additional content needs to be retrieved.
	 * For example the content of a {@link Binary} resource might not be stored in the json column.
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 * @throws SQLException
	 */
	protected void modifySearchResultResource(R resource, Connection connection) throws SQLException
	{
	}

	private void getResources(ResultSet result, int columnIndex, List<? super Resource> includeResources,
			Connection connection, DbSearchQuery query) throws SQLException
	{
		String json = result.getString(columnIndex);

		if (json == null)
			return;

		JsonArray array = (JsonArray) new JsonParser().parse(json);

		Iterator<JsonElement> it = array.iterator();
		while (it.hasNext())
		{
			JsonElement jsonElement = it.next();
			IBaseResource resource = getJsonParser().parseResource(jsonElement.toString());
			if (resource instanceof Resource)
			{
				query.modifyIncludeResource((Resource) resource, columnIndex, connection);
				includeResources.add((Resource) resource);
			}
			else
				logger.warn("parsed resouce of type {} not instance of {}, ignoring include resource",
						resource.getClass().getName(), Resource.class.getName());
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final SearchQuery<R> createSearchQuery(int page, int count)
	{
		return SearchQueryBuilder.create(resourceType, getResourceTable(), getResourceColumn(), page, count)
				.with(new ResourceId(getResourceIdColumn()), new ResourceLastUpdated(getResourceColumn()))
				.with(searchParameterFactories.stream().map(Supplier::get).toArray(SearchQueryParameter[]::new))
				.build();
	}
}
