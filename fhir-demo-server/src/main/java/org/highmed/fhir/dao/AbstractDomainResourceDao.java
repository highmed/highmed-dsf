package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.search.PartialResult;
import org.highmed.fhir.dao.search.SearchQueryFactory;
import org.highmed.fhir.dao.search.SearchQueryFactory.SearchQueryFactoryBuilder;
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
	private final Class<R> resourceType;

	private final String resourceTable;
	private final String resourceColumn;
	private final String resourceIdColumn;

	private final String resourceTypeName;
	private final IParser jsonParser;

	public AbstractDomainResourceDao(BasicDataSource dataSource, FhirContext fhirContext, Class<R> resourceType,
			String resourceTable, String resourceColumn, String resourceIdColumn)
	{
		this.dataSource = dataSource;
		this.resourceType = resourceType;

		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;
		this.resourceIdColumn = resourceIdColumn;

		resourceTypeName = Objects.requireNonNull(resourceType, "resourceType").getAnnotation(ResourceDef.class).name();
		jsonParser = Objects.requireNonNull(fhirContext, "fhirContext").newJsonParser();
		jsonParser.setStripVersionsFromReferences(false);
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

	public final R create(R resource) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			R inserted = create(connection, resource, UUID.randomUUID().toString());

			logger.debug("{} with ID {} created", resourceTypeName, inserted.getId());
			return inserted;
		}
	}

	private R create(Connection connection, R resource, String idPart) throws SQLException
	{
		resource = copy(resource);
		resource.setIdElement(new IdType(resourceTypeName, idPart, "1"));
		resource.getMeta().setVersionId("1");
		resource.getMeta().setLastUpdated(new Date());

		// db version set by default value
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO " + resourceTable + " (" + resourceIdColumn + ", " + resourceColumn + ") VALUES (?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(resource.getIdElement()));
			statement.setObject(2, resourceToPgObject(resource));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	protected abstract R copy(R resource);

	private Object resourceToPgObject(R resource)
	{
		if (resource == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("JSONB");
			o.setValue(jsonParser.encodeResourceToString(resource));
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Object uuidToPgObject(IdType id)
	{
		if (id == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(id.getIdPart());
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
		return jsonParser.parseResource(resourceType, json);
	}

	public final Optional<R> read(IdType id) throws SQLException, ResourceDeletedException
	{
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + ", deleted FROM "
						+ resourceTable + " WHERE " + resourceIdColumn + " = ? ORDER BY version LIMIT 1"))
		{
			statement.setObject(1, uuidToPgObject(id));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					if (result.getBoolean(2))
					{
						logger.info("{} with IdPart {} found, but marked as deleted.", resourceTypeName,
								id.getIdPart());
						throw new ResourceDeletedException(id);
					}
					else
					{
						logger.info("{} with IdPart {} found.", resourceTypeName, id.getIdPart());
						return Optional.of(getResource(result, 1));
					}
				}
				else
					return Optional.empty();
			}
		}
	}

	public final Optional<R> readVersion(IdType id) throws SQLException
	{
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT " + resourceColumn + " FROM "
						+ resourceTable + " WHERE " + resourceIdColumn + " = ? AND version = ?"))
		{
			statement.setObject(1, uuidToPgObject(id));
			statement.setLong(2, id.getVersionIdPartAsLong());

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					logger.info("{} with IdPart {} and Version {} found.", resourceTypeName, id.getIdPart(),
							id.getVersionIdPartAsLong());
					return Optional.of(getResource(result, 1));
				}
				else
				{
					logger.info("{} with IdPart {} and Version {} not found.", resourceTypeName, id.getIdPart(),
							id.getVersionIdPartAsLong());
					return Optional.empty();
				}
			}
		}
	}

	public final R update(R resource) throws SQLException, ResourceNotFoundException
	{
		Objects.requireNonNull(resource, "resource");

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			connection.setAutoCommit(false);

			try
			{
				long version = getLastVersion(resource, connection) + 1;
				R inserted = update(connection, copy(resource), resource.getIdElement().getIdPart(), version);

				connection.commit();

				logger.debug("{} with IdPart {} updated, new version {}.", resourceTypeName,
						inserted.getIdElement().getIdPart(), version);
				return inserted;
			}
			catch (Exception e)
			{
				connection.rollback();
				throw e;
			}
		}
	}

	private R update(Connection connection, R resource, String idPart, long version) throws SQLException
	{
		String versionAsString = String.valueOf(version);

		resource = copy(resource);
		resource.setIdElement(new IdType(resourceTypeName, idPart, versionAsString));
		resource.getMeta().setVersionId(versionAsString);
		resource.getMeta().setLastUpdated(new Date());

		try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + resourceTable + " ("
				+ resourceIdColumn + ", version, " + resourceColumn + ") VALUES (?, ?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(resource.getIdElement()));
			statement.setLong(2, version);
			statement.setObject(3, resourceToPgObject(resource));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	private long getLastVersion(R resource, Connection connection) throws SQLException, ResourceNotFoundException
	{
		try (PreparedStatement statement = connection.prepareStatement("SELECT version FROM " + resourceTable
				+ " WHERE " + resourceIdColumn + " = ? ORDER BY version LIMIT 1"))
		{
			statement.setObject(1, uuidToPgObject(resource.getIdElement()));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					long version = result.getLong(1);

					logger.debug("Latest version for {} with IdPart {} equals {}.", resourceTypeName,
							resource.getIdElement().getIdPart(), version);
					return version;
				}
				else
				{
					logger.debug("{} with IdPart {} not found.", resourceTypeName, resource.getIdElement().getIdPart());
					throw new ResourceNotFoundException(resource.getId());
				}
			}
		}
	}

	public final void delete(IdType id) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			try (PreparedStatement statement = connection.prepareStatement(
					"UPDATE " + resourceTable + " SET deleted = TRUE WHERE " + resourceIdColumn + " = ?"))
			{
				statement.setObject(1, uuidToPgObject(id));

				logger.trace("Executing query '{}'", statement);
				statement.execute();

				logger.debug("{} with ID {} marked as deleted.", resourceTypeName, id);
			}
		}
	}

	protected SearchQueryFactoryBuilder createSearchQueryFactory(int page, int count)
	{
		return SearchQueryFactoryBuilder.create(getResourceTable(), getResourceIdColumn(), getResourceColumn(), page,
				count);
	}

	protected PartialResult<R> search(SearchQueryFactory queryFactory) throws SQLException
	{
		try (Connection connection = getDataSource().getConnection())
		{
			int overallCount = 0;
			try (PreparedStatement statement = connection.prepareStatement(queryFactory.createCountSql()))
			{
				queryFactory.modifyStatement(statement);

				logger.trace("Executing query '{}'", statement);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
						overallCount = result.getInt(1);
				}
			}

			List<R> partialResult = new ArrayList<>();

			if (!queryFactory.isCountOnly(overallCount))
			{
				try (PreparedStatement statement = connection.prepareStatement(queryFactory.createSearchSql()))
				{
					queryFactory.modifyStatement(statement);

					logger.trace("Executing query '{}'", statement);
					try (ResultSet result = statement.executeQuery())
					{
						while (result.next())
							partialResult.add(getResource(result, 1));

					}
				}
			}

			return new PartialResult<>(overallCount, queryFactory.getPageAndCount(), partialResult,
					queryFactory.isCountOnly(overallCount));
		}
	}
}
