package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
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

public abstract class AbstractDao<D extends DomainResource> implements BasicCrudDao<D>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractDao.class);

	private static final long FIRST_VERSION = 1L;

	private final BasicDataSource dataSource;
	private final Class<D> resourceType;

	private final String resourceTable;
	private final String resourceColumn;
	private final String resourceIdColumn;

	private final String resourceTypeName;
	private final IParser jsonParser;

	public AbstractDao(BasicDataSource dataSource, FhirContext fhirContext, Class<D> resourceType, String resourceTable,
			String resourceColumn, String resourceIdColumn)
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

	@Override
	public D create(D resource) throws SQLException
	{
		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			D inserted = insert(connection, resource, UUID.randomUUID().toString(), FIRST_VERSION);

			logger.debug("{} with ID {} created", resourceTypeName, inserted.getId());
			return inserted;
		}
	}

	protected abstract D copy(D resource);

	private D insert(Connection connection, D resource, String idPart, long version) throws SQLException
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

	private Object resourceToPgObject(D resource)
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

	@Override
	public Optional<D> read(IdType id) throws SQLException, ResourceDeletedException
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
						logger.info("{} with IdPart {} found, but marked as deleted.", resourceTypeName, id.getIdPart());
						throw new ResourceDeletedException(id);
					}
					else
					{
						logger.info("{} with IdPart {} found.", resourceTypeName, id.getIdPart());
						return Optional.of(jsonParser.parseResource(resourceType, result.getString(1)));
					}
				}
				else
					return Optional.empty();
			}
		}
	}

	@Override
	public Optional<D> readVersion(IdType id) throws SQLException
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
					return Optional.of(jsonParser.parseResource(resourceType, result.getString(1)));
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

	@Override
	public D update(D resource) throws SQLException, ResourceNotFoundException
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
				D inserted = insert(connection, copy(resource), resource.getIdElement().getIdPart(), version);

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

	private long getLastVersion(D resource, Connection connection) throws SQLException, ResourceNotFoundException
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

	@Override
	public void delete(IdType id) throws SQLException
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
}
