package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyEnrollment;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyIdentifier;
import org.highmed.dsf.fhir.search.parameters.ResearchStudyPrincipalInvestigator;
import org.highmed.dsf.fhir.search.parameters.user.ResearchStudyUserFilter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDaoJdbc extends AbstractResourceDaoJdbc<ResearchStudy> implements ResearchStudyDao
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyDaoJdbc.class);

	public ResearchStudyDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ResearchStudy.class, "research_studies", "research_study", "research_study_id",
				ResearchStudyUserFilter::new, with(ResearchStudyIdentifier::new, ResearchStudyEnrollment::new,
						ResearchStudyPrincipalInvestigator::new),
				with());
	}

	@Override
	protected ResearchStudy copy(ResearchStudy resource)
	{
		return resource.copy();
	}

	@Override
	public boolean existsByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(
			Connection connection, IdType principalInvestigatorId, OrganizationType organizationType,
			IdType organizationId) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(principalInvestigatorId, "principalInvestigatorId");
		Objects.requireNonNull(organizationType, "organizationType");
		Objects.requireNonNull(organizationId, "organizationId");

		try (PreparedStatement statement = connection
				.prepareStatement("SELECT COUNT(*) FROM current_research_studies WHERE "
						+ "(research_study->'principalInvestigator'->>'reference' = ? OR research_study->'principalInvestigator'->>'reference' = ?) AND "
						+ "(research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb)"))
		{
			statement.setString(1, principalInvestigatorId.getValue());
			statement.setString(2, principalInvestigatorId.toVersionless().getValue());

			setOrganization(statement, organizationType, organizationId);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					return result.getInt(1) > 0;
				else
					return false;
			}
		}
	}

	@Override
	public boolean existsByEnrollmentIdAndOrganizationTypeAndOrganizationIdWithTransaction(Connection connection,
			IdType enrollmentId, OrganizationType organizationType, IdType organizationId) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(enrollmentId, "enrollmentId");
		Objects.requireNonNull(organizationType, "organizationType");
		Objects.requireNonNull(organizationId, "organizationId");

		try (PreparedStatement statement = connection
				.prepareStatement("SELECT COUNT(*) FROM current_research_studies WHERE "
						+ "(? IN (SELECT enrollment->>'reference' FROM jsonb_array_elements(research_study->'enrollment') AS enrollment) OR "
						+ "? IN (SELECT enrollment->>'reference' FROM jsonb_array_elements(research_study->'enrollment') AS enrollment)) AND "
						+ "(research_study->'extension' @> ?::jsonb OR research_study->'extension' @> ?::jsonb)"))
		{
			statement.setString(1, enrollmentId.getValue());
			statement.setString(2, enrollmentId.toVersionless().getValue());

			setOrganization(statement, organizationType, organizationId);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					return result.getInt(1) > 0;
				else
					return false;
			}
		}
	}

	private void setOrganization(PreparedStatement statement, OrganizationType organizationType, IdType organizationId)
			throws SQLException
	{
		switch (organizationType)
		{
			case MeDIC:
				statement.setString(3,
						"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
								+ organizationId.getValue() + "\"}}]");
				statement.setString(4,
						"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-medic\",\"valueReference\":{\"reference\":\""
								+ organizationId.toVersionless().getValue() + "\"}}]");
				break;

			case TTP:
				statement.setString(3,
						"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
								+ organizationId.getValue() + "\"}}]");
				statement.setString(4,
						"[{\"url\":\"http://highmed.org/fhir/StructureDefinition/participating-ttp\",\"valueReference\":{\"reference\":\""
								+ organizationId.toVersionless().getValue() + "\"}}]");
				break;
		}
	}
}
