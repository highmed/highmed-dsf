package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SearchQuery
{
	boolean isDefined();
	
	String getSubquery();

	int getSqlParameterCount();

	void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException;
}
