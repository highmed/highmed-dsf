package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.UserRole;
import org.hl7.fhir.r4.model.ActivityDefinition;

public interface ActivityDefinitionProvider
{
	Optional<ActivityDefinition> getActivityDefinition(Connection connection, UserRole userRole, String processUrl,
			String processVersion, String messageName);
}
