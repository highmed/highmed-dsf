package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class ProvenanceUserFilter extends AbstractUserFilter
{
	public ProvenanceUserFilter(User user)
	{
		super(user);
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
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		// nothing to do
	}
}
