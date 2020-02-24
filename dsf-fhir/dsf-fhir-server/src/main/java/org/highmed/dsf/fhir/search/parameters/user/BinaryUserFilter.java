package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class BinaryUserFilter extends AbstractUserFilter
{
	public BinaryUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			return "(binary_json->'securityContext'->>'reference' = ? OR binary_json->'securityContext'->>'reference' = ?)";
	}

	@Override
	public int getSqlParameterCount()
	{
		return UserRole.LOCAL.equals(user.getRole()) ? 0 : 2;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		if (!UserRole.LOCAL.equals(user.getRole()))
		{
			if (parameterIndex == 1)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
			else if (parameterIndex == 2)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		}
	}
}
