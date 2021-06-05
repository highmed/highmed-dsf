package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.OrganizationAffiliation;

public interface OrganizationAffiliationDao extends ResourceDao<OrganizationAffiliation>
{
	List<OrganizationAffiliation> readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
			Connection connection, String identifierValue) throws SQLException;
}
