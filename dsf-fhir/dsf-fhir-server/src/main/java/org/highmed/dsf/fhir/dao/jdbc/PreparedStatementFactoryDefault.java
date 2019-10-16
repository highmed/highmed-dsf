package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;

class PreparedStatementFactoryDefault<R extends Resource> extends AbstractPreparedStatementFactory<R>
{
	PreparedStatementFactoryDefault(FhirContext fhirContext, Class<R> resourceType, String resourceTable,
			String resourceIdColumn, String resourceColumn)
	{
		super(fhirContext, resourceType, createSql(resourceTable, resourceIdColumn, resourceColumn),
				readByIdSql(resourceTable, resourceIdColumn, resourceColumn),
				readByIdAndVersionSql(resourceTable, resourceIdColumn, resourceColumn),
				updateNewRowSql(resourceTable, resourceIdColumn, resourceColumn),
				updateSameRowSql(resourceTable, resourceIdColumn, resourceColumn));
	}

	private static String createSql(String resourceTable, String resourceIdColumn, String resourceColumn)
	{
		return "INSERT INTO " + resourceTable + " (" + resourceIdColumn + ", " + resourceColumn + ") VALUES (?, ?)";
	}

	private static String readByIdSql(String resourceTable, String resourceIdColumn, String resourceColumn)
	{
		return "SELECT deleted, " + resourceColumn + " FROM " + resourceTable + " WHERE " + resourceIdColumn
				+ " = ? ORDER BY version DESC LIMIT 1";
	}

	private static String readByIdAndVersionSql(String resourceTable, String resourceIdColumn, String resourceColumn)
	{
		return "SELECT " + resourceColumn + " FROM " + resourceTable + " WHERE " + resourceIdColumn
				+ " = ? AND version = ?";
	}

	private static String updateNewRowSql(String resourceTable, String resourceIdColumn, String resourceColumn)
	{
		return "INSERT INTO " + resourceTable + " (" + resourceIdColumn + ", version, " + resourceColumn
				+ ") VALUES (?, ?, ?)";
	}

	private static String updateSameRowSql(String resourceTable, String resourceIdColumn, String resourceColumn)
	{
		return "UPDATE " + resourceTable + " SET " + resourceColumn + " = ? WHERE " + resourceIdColumn
				+ " = ? AND version = ?";
	}

	@Override
	public void configureCreateStatement(PreparedStatement statement, R resource, UUID uuid) throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
		statement.setObject(2, resourceToPgObject(resource));
	}

	@Override
	public void configureReadByIdStatement(PreparedStatement statement, UUID uuid) throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
	}

	@Override
	public boolean isReadByIdDeleted(ResultSet result) throws SQLException
	{
		return result.getBoolean(1);
	}

	@Override
	public R getReadByIdResource(ResultSet result) throws SQLException
	{
		String json = result.getString(2);

		return jsonToResource(json);
	}

	@Override
	public void configureReadByIdAndVersionStatement(PreparedStatement statement, UUID uuid, long version)
			throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
		statement.setLong(2, version);
	}

	@Override
	public R getReadByIdAndVersionResource(ResultSet result) throws SQLException
	{
		String json = result.getString(1);

		return jsonToResource(json);
	}

	@Override
	public void configureUpdateNewRowSqlStatement(PreparedStatement statement, UUID uuid, long version, R resource)
			throws SQLException
	{
		statement.setObject(1, uuidToPgObject(uuid));
		statement.setLong(2, version);
		statement.setObject(3, resourceToPgObject(resource));
	}

	@Override
	public void configureUpdateSameRowSqlStatement(PreparedStatement statement, UUID uuid, long version, R resource)
			throws SQLException
	{
		statement.setObject(1, resourceToPgObject(resource));
		statement.setObject(2, uuidToPgObject(uuid));
		statement.setLong(3, version);
	}
}
