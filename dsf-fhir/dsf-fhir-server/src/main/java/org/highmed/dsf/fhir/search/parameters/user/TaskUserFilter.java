package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;

public class TaskUserFilter extends AbstractUserFilter
{
	private static final String RESOURCE_COLUMN = "task";

	private final String resourceColumn;

	public TaskUserFilter(User user)
	{
		super(user, null, null);

		this.resourceColumn = RESOURCE_COLUMN;
	}

	public TaskUserFilter(User user, String resourceColumn)
	{
		super(user, null, null);

		this.resourceColumn = resourceColumn;
	}

	@Override
	public String getFilterQuery()
	{
		// TODO modify for requester = Practitioner or PractitionerRole
		return "(" + resourceColumn + "->'requester'->>'reference' = ? OR " + resourceColumn
				+ "->'requester'->>'reference' = ? OR " + resourceColumn
				+ "->'restriction'->'recipient' @> ?::jsonb OR " + resourceColumn
				+ "->'restriction'->'recipient' @> ?::jsonb)";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 4;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		if (subqueryParameterIndex == 1)
			statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
		else if (subqueryParameterIndex == 2)
			statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		else if (subqueryParameterIndex == 3)
			statement.setString(parameterIndex,
					"[{\"reference\": \"" + user.getOrganization().getIdElement().getValue() + "\"}]");
		else if (subqueryParameterIndex == 4)
			statement.setString(parameterIndex,
					"[{\"reference\": \"" + user.getOrganization().getIdElement().toVersionless().getValue() + "\"}]");
	}
}
