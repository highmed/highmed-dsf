package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class GroupUserFilter extends AbstractUserFilter
{
	private static final String RESOURCE_COLUMN = "group";

	public GroupUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public GroupUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			return "(concat('Group/', " + resourceColumn + "->>'id') IN "
					+ "(SELECT enrollment->>'reference' FROM (SELECT jsonb_array_elements(research_study->'enrollment') AS enrollment FROM current_research_studies "
					+ "WHERE research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb) AS enrollments))";
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
			switch (user.getOrganizationType())
			{
				case MeDIC:
					if (subqueryParameterIndex == 1)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;

				case TTP:
					if (subqueryParameterIndex == 1)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
			}

			if (subqueryParameterIndex == 3)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
			else if (subqueryParameterIndex == 4)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		}
	}
}
