package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.highmed.dsf.fhir.OrganizationType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResearchStudy;

public interface ResearchStudyDao extends ResourceDao<ResearchStudy>
{
	List<ResearchStudy> readByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(
			Connection connection, IdType principalInvestigatorId, OrganizationType organizationType,
			IdType organizationId) throws SQLException;
}
