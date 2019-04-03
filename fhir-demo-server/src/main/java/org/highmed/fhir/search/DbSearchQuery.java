package org.highmed.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DbSearchQuery
{
	boolean isCountOnly(int overallCount);

	String getCountSql();

	String getSearchSql();

	void modifyStatement(PreparedStatement statement) throws SQLException;

	PageAndCount getPageAndCount();
}
