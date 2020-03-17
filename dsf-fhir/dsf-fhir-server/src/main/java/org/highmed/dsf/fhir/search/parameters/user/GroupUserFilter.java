package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class GroupUserFilter extends AbstractUserFilter
{
	public GroupUserFilter(User user)
	{
		super(user);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			return "(concat('Group/', group->>'id') IN "
					+ "(SELECT enrollment->>'reference' FROM (SELECT jsonb_array_elements(research_study->'enrollment') AS enrollment FROM current_research_studies "
					+ "WHERE research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb) AS enrollments))";
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
			switch (user.getOrganizationType())
			{
				case MeDIC:
					if (parameterIndex == 1)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (parameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;

				case TTP:
					if (parameterIndex == 1)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (parameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
			}

			if (parameterIndex == 3)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().getValue());
			else if (parameterIndex == 4)
				statement.setString(parameterIndex, user.getOrganization().getIdElement().toVersionless().getValue());
		}
	}
}
