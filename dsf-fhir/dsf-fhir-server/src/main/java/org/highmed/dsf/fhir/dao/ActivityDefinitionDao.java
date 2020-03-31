package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.ActivityDefinition;

public interface ActivityDefinitionDao extends ResourceDao<ActivityDefinition>, ReadByUrlDao<ActivityDefinition>
{
	Optional<ActivityDefinition> readByOrganizationTypeUserRoleProcessUrlVersionMessageNameAndNotRetiredWithTransaction(
			Connection connection, OrganizationType requesterOrganizationType,
			OrganizationType recipientOrganizationType, UserRole userRole, String processUrl, String processVersion,
			String messageName) throws SQLException;
}
