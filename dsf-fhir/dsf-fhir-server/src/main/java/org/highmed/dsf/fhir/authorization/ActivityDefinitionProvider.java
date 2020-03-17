package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.ActivityDefinition;

public interface ActivityDefinitionProvider
{
	Optional<ActivityDefinition> getActivityDefinition(Connection connection, User user, String processUrl,
			String processVersion, String messageName);
}
