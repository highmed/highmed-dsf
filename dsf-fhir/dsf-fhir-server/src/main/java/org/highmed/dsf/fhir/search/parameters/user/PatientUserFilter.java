package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class PatientUserFilter extends AbstractUserFilter
{
	private static final String RESOURCE_COLUMN = "patient";

	public PatientUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public PatientUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return ""; // not filtered for local users
		else
			return "false";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 0;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		// nothing to do
	}
}
