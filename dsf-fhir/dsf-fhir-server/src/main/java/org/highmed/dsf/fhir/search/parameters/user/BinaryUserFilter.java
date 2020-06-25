package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class BinaryUserFilter extends AbstractUserFilter
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
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			return "(" + resourceColumn + "->'securityContext'->>'reference' = ? OR " + resourceColumn
					+ "->'securityContext'->>'reference' = ?)";
	}

	@Override
	public int getSqlParameterCount()
	{
		return UserRole.LOCAL.equals(user.getRole()) ? 0 : 2;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		if (!UserRole.LOCAL.equals(user.getRole()))
		{
			if (subqueryParameterIndex == 1)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
			else if (subqueryParameterIndex == 2)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		}
	}
}
