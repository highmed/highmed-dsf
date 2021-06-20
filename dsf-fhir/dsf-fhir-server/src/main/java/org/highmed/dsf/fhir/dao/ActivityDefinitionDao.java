package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.ActivityDefinition;

public interface ActivityDefinitionDao extends ResourceDao<ActivityDefinition>, ReadByUrlDao<ActivityDefinition>
{
	Optional<ActivityDefinition> readByProcessUrlVersionAndStatusDraftOrActiveWithTransaction(Connection connection,
			String processUrl, String processVersion) throws SQLException;
}
