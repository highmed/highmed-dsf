package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class TaskUserFilter extends AbstractUserFilter
{
	public TaskUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user);
	}

	@Override
	public String getFilterQuery()
	{
		return "(task->'requester'->>'reference' = ? OR task->'requester'->>'reference' = ? OR"
				+ " task->'restriction'->'recipient' @> ?::jsonb OR task->'restriction'->'recipient' @> ?::jsonb)";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 4;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		if (parameterIndex == 1)
			statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
		else if (parameterIndex == 2)
			statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		else if (parameterIndex == 3)
			statement.setString(parameterIndex,
					"[{\"reference\": \"" + user.getOrganization().getIdElement().getValue() + "\"}]");
		else if (parameterIndex == 4)
			statement.setString(parameterIndex,
					"[{\"reference\": \"" + user.getOrganization().getIdElement().toVersionless().getValue() + "\"}]");
	}
}
