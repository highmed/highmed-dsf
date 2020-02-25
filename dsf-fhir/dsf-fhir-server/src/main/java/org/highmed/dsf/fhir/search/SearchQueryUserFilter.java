package org.highmed.dsf.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SearchQueryUserFilter
{
	String AUTHORIZATION_ROLE_SYSTEM = "http://highmed.org/fhir/CodeSystem/authorization-role";
	String AUTHORIZATION_ROLE_VALUE_REMOTE = "REMOTE";
	String AUTHORIZATION_ROLE_VALUE_LOCAL = "LOCAL";

	String getFilterQuery();

	int getSqlParameterCount();
	
	void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException;
}
