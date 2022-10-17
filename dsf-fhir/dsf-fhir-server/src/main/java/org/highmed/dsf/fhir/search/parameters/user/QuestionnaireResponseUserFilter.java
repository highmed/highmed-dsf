package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class QuestionnaireResponseUserFilter extends AbstractUserFilter
{
	public QuestionnaireResponseUserFilter(User user)
	{
		super(user, null, null);
	}

	@Override
	public String getFilterQuery()
	{
		// read allowed for local users
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";

		// read not allowed for non local users
		else
			return "FALSE";
	}

	@Override
	public int getSqlParameterCount()
	{
		// no parameters
		return 0;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		// no parameters to modify
	}
}
