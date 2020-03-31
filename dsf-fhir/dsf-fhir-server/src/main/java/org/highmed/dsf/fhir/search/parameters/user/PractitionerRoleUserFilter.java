package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class PractitionerRoleUserFilter extends AbstractUserFilter
{
	public PractitionerRoleUserFilter(User user)
	{
		super(user);
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
			return "(practitioner_role->'organization'->>'reference' = ? OR practitioner_role->'organization'->>'reference' = ? OR "
					+ "practitioner_role->'practitioner'->>'reference' IN (SELECT research_study->'principalInvestigator'->>'reference' FROM "
					+ "current_research_studies WHERE current_research_studies WHERE research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb))";
	}

	@Override
	public int getSqlParameterCount()
	{
		return UserRole.LOCAL.equals(user.getRole()) ? 0 : 4;
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

			switch (user.getOrganizationType())
			{
				case MeDIC:
					if (parameterIndex == 3)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (parameterIndex == 4)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
					
				case TTP:
					if (parameterIndex == 3)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().getValue() + "\"}}]");
					else if (parameterIndex == 4)
						statement.setString(parameterIndex,
								"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
										+ user.getOrganization().getIdElement().toVersionless().getValue() + "\"}}]");
					break;
			}
		}
	}
}
