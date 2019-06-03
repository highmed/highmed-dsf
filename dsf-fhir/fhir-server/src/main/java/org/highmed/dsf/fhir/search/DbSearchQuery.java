package org.highmed.dsf.fhir.search;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;

public interface DbSearchQuery
{
	boolean isCountOnly(int overallCount);

	String getCountSql();

	String getSearchSql();

	void modifyStatement(PreparedStatement statement, BiFunctionWithSqlException<String, Object[], Array> arrayCreator)
			throws SQLException;

	PageAndCount getPageAndCount();
}
