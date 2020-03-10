package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.highmed.dsf.fhir.OrganizationType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResearchStudy;

public interface ResearchStudyDao extends ResourceDao<ResearchStudy>
{
	boolean existsByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(Connection connection,
			IdType principalInvestigatorId, OrganizationType organizationType, IdType organizationId)
			throws SQLException;

	boolean existsByEnrollmentIdAndOrganizationTypeAndOrganizationIdWithTransaction(Connection connection,
			IdType enrollmentId, OrganizationType organizationType, IdType organizationId) throws SQLException;
}
