package org.highmed.dsf.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SearchQueryUserFilter
{
	/**
	 * @return not <code>null</code>, empty {@link String} if resources should not be filtered
	 */
	String getFilterQuery();

	/**
	 * @return &gt;= 0, 0 if {@link #getFilterQuery()} returns empty {@link String}
	 */
	int getSqlParameterCount();

	/**
	 * @param parameterIndex
	 *            >= 1
	 * @param subqueryParameterIndex
	 *            [1 ... {@link #getSqlParameterCount()}]
	 * @param statement
	 * @throws SQLException
	 */
	void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException;
}
