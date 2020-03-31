package org.highmed.dsf.fhir.search.parameters.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;

public class PractitionerUserFilter extends AbstractUserFilter
{
	public PractitionerUserFilter(User user)
	{
		super(user);
	}

	@Override
	public String getFilterQuery()
	{
		if (UserRole.LOCAL.equals(user.getRole()))
			return "";
		else
			// Practitioner part of ResearchStudy as principal investigator and users Organization part of same
			// ResearchStudy or
			return "(concat('Practitioner/', practitioner->>'id') IN ("
					+ "SELECT research_study->'principalInvestigator'->>'reference' FROM current_research_studies WHERE "
					+ "research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb) OR "

					// Practitioner part of PractitionerRole and users Organization part of same PractitionerRole
					+ "concat('Practitioner/', practitioner->>'id') IN ("
					+ "SELECT practitioner_role->'practitioner'->>'reference' FROM current_practitioner_roles WHERE "
					+ "practitioner_role->'organization'->>'reference' = ? OR practitioner_role->'organization'->>'reference' = ?))";
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
