package org.highmed.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hl7.fhir.r4.model.DomainResource;

public interface DbSearchQuery
{
	boolean isCountOnly(int overallCount);

	String getCountSql();

	String getSearchSql();

	void modifyStatement(PreparedStatement statement) throws SQLException;

	PageAndCount getPageAndCount();

	Class<? extends DomainResource> getIncludeResourceTypForColumName(String columnName);
}
