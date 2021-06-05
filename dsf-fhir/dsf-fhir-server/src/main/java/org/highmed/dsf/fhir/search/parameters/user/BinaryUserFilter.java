package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;

public class BinaryUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "binary_json";

	public BinaryUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public BinaryUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		String baseQuery = getFilterQueryBase(resourceColumn);

		String addQuery = " OR (SELECT resource FROM history WHERE resource IS NOT NULL"
				+ " AND ((type || '/' || id) = " + resourceColumn + "->'securityContext'->>'reference'"
				+ " OR (type || '/' || id || '/_history/' || version) = " + resourceColumn
				+ "->'securityContext'->>'reference')" + " AND (" + getFilterQueryBase("resource")
				+ ") ORDER BY version DESC LIMIT 1) IS NOT NULL";

		return "(" + baseQuery + addQuery + ")";
	}

	@Override
	public int getSqlParameterCount()
	{
		return super.getSqlParameterCount() * 2;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		super.modifyStatement(parameterIndex, ((subqueryParameterIndex - 1) % super.getSqlParameterCount()) + 1,
				statement);
	}
}
