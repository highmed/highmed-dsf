package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.converter.SnapshotInfoConverter;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.service.SnapshotInfo;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDao extends AbstractStructureDefinitionDao
{
	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionSnapshotDao.class);

	private final SnapshotInfoConverter converter;

	public StructureDefinitionSnapshotDao(BasicDataSource dataSource, FhirContext fhirContext,
			SnapshotInfoConverter converter)
	{
		super(dataSource, fhirContext, "structure_definition_snapshots", "structure_definition_snapshot",
				"structure_definition_snapshot_id");

		this.converter = converter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(converter, "converter");
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}

	public StructureDefinition create(UUID uuid, StructureDefinition resource, SnapshotInfo info) throws SQLException
	{
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(info, "info");

		try (Connection connection = getDataSource().getConnection())
		{
			connection.setReadOnly(false);

			StructureDefinition inserted = create(connection, resource, info, uuid);

			logger.debug("{} with ID {} created", getResourceTypeName(), inserted.getId());
			return inserted;
		}
	}

	private StructureDefinition create(Connection connection, StructureDefinition resource, SnapshotInfo info,
			UUID uuid) throws SQLException
	{
		resource = copy(resource);
		resource.setIdElement(new IdType(getResourceTypeName(), uuid.toString(), "1"));
		resource.getMeta().setVersionId("1");
		resource.getMeta().setLastUpdated(new Date());

		// db version set by default value
		try (PreparedStatement statement = connection
				.prepareStatement("INSERT INTO " + getResourceTable() + " (" + getResourceIdColumn() + ", "
						+ getResourceColumn() + ", structure_definition_snapshot_info) VALUES (?, ?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(uuid));
			statement.setObject(2, resourceToPgObject(resource));
			statement.setObject(3, converter.toDb(info));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	public StructureDefinition update(StructureDefinition resource, SnapshotInfo info)
			throws SQLException, ResourceNotFoundException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(info, "info");

		try (Connection connection = getDataSource().getConnection())
		{
			connection.setReadOnly(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			connection.setAutoCommit(false);

			try
			{
				LatestVersion latestVersion = getLatestVersion(resource, connection);
				long newVersion = latestVersion.version + 1;

				StructureDefinition updated = update(connection, resource, info, newVersion);
				if (latestVersion.deleted)
					markDeleted(connection, toUuid(updated.getIdElement().getIdPart()), false);

				connection.commit();

				logger.debug("{} with IdPart {} updated, new version {}.", getResourceTypeName(),
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

	private StructureDefinition update(Connection connection, StructureDefinition resource, SnapshotInfo info,
			long newVersion) throws SQLException
	{
		UUID uuid = toUuid(resource.getIdElement().getIdPart());
		if (uuid == null)
			throw new IllegalArgumentException("resource.id is not a UUID");

		String newVersionAsString = String.valueOf(newVersion);
		IdType newId = new IdType(getResourceTypeName(), resource.getIdElement().getIdPart(), newVersionAsString);

		resource = copy(resource);
		resource.setIdElement(newId);
		resource.getMeta().setVersionId(newVersionAsString);
		resource.getMeta().setLastUpdated(new Date());

		try (PreparedStatement statement = connection
				.prepareStatement("INSERT INTO " + getResourceTable() + " (" + getResourceIdColumn() + ", version, "
						+ getResourceColumn() + ", structure_definition_snapshot_info) VALUES (?, ?, ?, ?)"))
		{
			statement.setObject(1, uuidToPgObject(uuid));
			statement.setLong(2, newVersion);
			statement.setObject(3, resourceToPgObject(resource));
			statement.setObject(4, converter.toDb(info));

			logger.trace("Executing query '{}'", statement);
			statement.execute();
		}

		return resource;
	}

	public void deleteAllByDependency(String url) throws SQLException
	{
		Objects.requireNonNull(url, "url");

		try (Connection connection = getDataSource().getConnection())
		{
			connection.setReadOnly(false);

			try (PreparedStatement statement = connection.prepareStatement("UPDATE " + getResourceTable()
					+ " SET deleted = TRUE WHERE structure_definition_snapshot_info->'dependencies'->'profiles' ?? ?"))
			{
				statement.setString(1, url);

				logger.trace("Executing query '{}'", statement);
				int count = statement.executeUpdate();

				logger.debug("{} {} snapshot{} with dependency url {} marked as deleted", count, getResourceTypeName(),
						count != 1 ? "s" : "", url);
			}
		}
	}
}
