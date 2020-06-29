package org.highmed.dsf.fhir.dao.jdbc;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hl7.fhir.r4.model.Binary;

import ca.uhn.fhir.context.FhirContext;

class PreparedStatementFactoryBinary extends AbstractPreparedStatementFactory<Binary>
{
	private static final String createSql = "INSERT INTO binaries (binary_id, binary_json, binary_data) VALUES (?, ?, ?)";
	private static final String readByIdSql = "SELECT deleted, version, binary_json, binary_data FROM binaries WHERE binary_id = ? ORDER BY version DESC LIMIT 1";
	private static final String readByIdAndVersionSql = "SELECT deleted, version, binary_json, binary_data FROM binaries WHERE binary_id = ? AND (version = ? OR version = ?) ORDER BY version DESC LIMIT 1";
	private static final String updateNewRowSql = "INSERT INTO binaries (binary_id, version, binary_json, binary_data) VALUES (?, ?, ?, ?)";
	private static final String updateSameRowSql = "UPDATE binaries SET binary_json = ?, binary_data = ? WHERE binary_id = ? AND version = ?";

	PreparedStatementFactoryBinary(FhirContext fhirContext)
	{
		super(fhirContext, Binary.class, createSql, readByIdSql, readByIdAndVersionSql, updateNewRowSql,
				updateSameRowSql);
	}

	@Override
	public void configureCreateStatement(PreparedStatement statement, Binary resource, UUID uuid) throws SQLException
	{
		byte[] data = resource.getData();
		resource.setData(null);

		statement.setObject(1, uuidToPgObject(uuid));
		statement.setObject(2, resourceToPgObject(resource));

		if (data != null)
			statement.setBinaryStream(3, new ByteArrayInputStream(data));
		else
			statement.setNull(3, Types.VARBINARY);

		resource.setData(data);
	}

	@Override
	public void configureReadByIdStatement(PreparedStatement statement, UUID uuid) throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
	}

	@Override
	public LocalDateTime getReadByIdDeleted(ResultSet result) throws SQLException
	{
		Timestamp deleted = result.getTimestamp(1);
		return deleted == null ? null : deleted.toLocalDateTime();
	}

	@Override
	public long getReadByIdVersion(ResultSet result) throws SQLException
	{
		return result.getLong(2);
	}

	@Override
	public Binary getReadByIdResource(ResultSet result) throws SQLException
	{
		String json = result.getString(3);
		byte[] data = result.getBytes(4);

		return jsonToResource(json).setData(data);
	}

	@Override
	public void configureReadByIdAndVersionStatement(PreparedStatement statement, UUID uuid, long version)
			throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
		statement.setLong(2, version);
		statement.setLong(3, version - 1);
	}

	@Override
	public LocalDateTime getReadByIdVersionDeleted(ResultSet result) throws SQLException
	{
		Timestamp deleted = result.getTimestamp(1);
		return deleted == null ? null : deleted.toLocalDateTime();
	}

	@Override
	public long getReadByIdVersionVersion(ResultSet result) throws SQLException
	{
		return result.getLong(2);
	}

	@Override
	public Binary getReadByIdAndVersionResource(ResultSet result) throws SQLException
	{
		String json = result.getString(3);
		byte[] data = result.getBytes(4);

		return jsonToResource(json).setData(data);
	}

	@Override
	public void configureUpdateNewRowSqlStatement(PreparedStatement statement, UUID uuid, long version, Binary resource)
			throws SQLException
	{
		byte[] data = resource.getData();
		resource.setData(null);

		statement.setObject(1, uuidToPgObject(uuid));
		statement.setLong(2, version);
		statement.setObject(3, resourceToPgObject(resource));

		if (data != null)
			statement.setBinaryStream(4, new ByteArrayInputStream(data));
		else
			statement.setNull(4, Types.VARBINARY);

		resource.setData(data);
	}

	@Override
	public void configureUpdateSameRowSqlStatement(PreparedStatement statement, UUID uuid, long version,
			Binary resource) throws SQLException
	{
		byte[] data = resource.getData();
		resource.setData(null);

		statement.setObject(1, resourceToPgObject(resource));

		if (data != null)
			statement.setBinaryStream(2, new ByteArrayInputStream(data));
		else
			statement.setNull(2, Types.VARBINARY);

		statement.setObject(3, uuidToPgObject(uuid));
		statement.setLong(4, version);

		resource.setData(data);
	}
}
