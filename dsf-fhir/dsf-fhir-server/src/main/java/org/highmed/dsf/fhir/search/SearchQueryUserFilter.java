package org.highmed.dsf.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SearchQueryUserFilter
{
	String AUTHORIZATION_ROLE_SYSTEM = "http://highmed.org/fhir/CodeSystem/authorization-role";
	String AUTHORIZATION_ROLE_VALUE_REMOTE = "REMOTE";
	String AUTHORIZATION_ROLE_VALUE_LOCAL = "LOCAL";

	/**
	 * @return not <code>null</code>, empty {@link String} if resources should not be filtered
	 */
	String getFilterQuery();

	/**
	 * @return &gt;= 0, 0 if {@link #getFilterQuery()} returns empty {@link String}
	 */
	int getSqlParameterCount();

	void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException;
}
