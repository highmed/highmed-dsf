package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.PageAndCount;

public interface DbSearchQuery
{
	boolean isCountOnly(int overallCount);

	String getCountSql();

	String getSearchSql();

	void modifyStatement(PreparedStatement statement) throws SQLException;

	PageAndCount getPageAndCount();
}
