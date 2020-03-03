package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ActivityDefinitionProviderImpl implements ActivityDefinitionProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionProviderImpl.class);

	private final ActivityDefinitionDao dao;
	private final OrganizationType organizationType;

	public ActivityDefinitionProviderImpl(ActivityDefinitionDao dao, OrganizationType organizationType)
	{
		this.dao = dao;
		this.organizationType = organizationType;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(organizationType, "organizationType");
	}

	@Override
	public Optional<ActivityDefinition> getActivityDefinition(Connection connection, UserRole userRole,
			String processUrl, String processVersion, String messageName)
	{
		try
		{
			Optional<ActivityDefinition> optAD = dao
					.readByOrganizationTypeUserRoleProcessUrlVersionMessageNameAndNotRetiredWithTransaction(connection,
							organizationType, userRole, processUrl, processVersion, messageName);

			if (optAD.isPresent())
				logger.debug(
						"ActivityDefinition with organization-type {}, user-role {}, process-url {}, process-version {} and message-name {} found",
						organizationType, userRole, processUrl, processVersion, messageName);
			else
				logger.warn(
						"ActivityDefinition with organization-type {}, user-role {}, process-url {}, process-version {} and message-name {} not found",
						organizationType, userRole, processUrl, processVersion, messageName);

			return optAD;
		}
		catch (SQLException e)
		{
			logger.warn("Error while reading ActivityDefinition", e);
			return Optional.empty();
		}
	}
}
