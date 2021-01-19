package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class PractitionerRoleUserFilter extends AbstractUserFilter
{
	private static String RESOURCE_COLUMN = "practitioner_role";

	public PractitionerRoleUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public PractitionerRoleUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			// user part of PractitionerRoles Organization or
			// PractitionerRoles Practitioner part of ResearchStudy as principal investigator and
			// users Organization part of ResearchStudy
			return "(" + resourceColumn + "->'organization'->>'reference' = ? OR " + resourceColumn
					+ "->'organization'->>'reference' = ? OR " + resourceColumn
					+ "->'practitioner'->>'reference' IN (SELECT research_study->'principalInvestigator'->>'reference' FROM "
					+ "current_research_studies WHERE current_research_studies WHERE research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb))";
	}

	@Override
	public int getSqlParameterCount()
	{
		return UserRole.LOCAL.equals(user.getRole()) ? 0 : 4;
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

			switch (user.getOrganizationType())
			{
				case MeDIC:
					if (subqueryParameterIndex == 3)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 4)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;

				case TTP:
					if (subqueryParameterIndex == 3)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (subqueryParameterIndex == 4)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
			}
		}
	}
}
