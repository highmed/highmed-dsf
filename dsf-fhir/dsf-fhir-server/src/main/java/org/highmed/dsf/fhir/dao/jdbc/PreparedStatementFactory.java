package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.hl7.fhir.r4.model.Resource;
import org.postgresql.util.PGobject;

interface PreparedStatementFactory<R extends Resource>
{
	PGobject uuidToPgObject(UUID uuid);

	String getCreateSql();

	void configureCreateStatement(PreparedStatement statement, R resource, UUID uuid) throws SQLException;

	String getReadByIdSql();

	void configureReadByIdStatement(PreparedStatement statement, UUID uuid) throws SQLException;

	boolean isReadByIdDeleted(ResultSet result) throws SQLException;

	R getReadByIdResource(ResultSet result) throws SQLException;

	String getReadByIdAndVersionSql();

	void configureReadByIdAndVersionStatement(PreparedStatement statement, UUID uuid, long version) throws SQLException;

	R getReadByIdAndVersionResource(ResultSet result) throws SQLException;

	String getUpdateNewRowSql();

	void configureUpdateNewRowSqlStatement(PreparedStatement statement, UUID uuid, long version, R resource)
			throws SQLException;

	String getUpdateSameRowSql();

	void configureUpdateSameRowSqlStatement(PreparedStatement statement, UUID uuid, long version, R resource)
			throws SQLException;
}
