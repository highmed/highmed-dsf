package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class ResearchStudyUserFilter extends AbstractUserFilter
{
	private static final String RESOURCE_COLUMN = "research_study";

	public ResearchStudyUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public ResearchStudyUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			return "(" + resourceColumn + "->'extension' @> ?::jsonb OR " + resourceColumn
					+ "->'extension' @> ?::jsonb)";
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
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;

				case TTP:
					if (subqueryParameterIndex == 1)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 2)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
			}
		}
	}
}
