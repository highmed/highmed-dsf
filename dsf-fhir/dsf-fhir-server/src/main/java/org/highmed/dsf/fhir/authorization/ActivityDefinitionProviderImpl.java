package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;
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
	public Optional<ActivityDefinition> getActivityDefinition(Connection connection, User user, String processUrl,
			String processVersion, String messageName)
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(user, "user");

		if (processUrl == null || processUrl.isBlank())
		{
			logger.warn("processUrl null or blank");
			return Optional.empty();
		}
		else if (processVersion == null || processVersion.isBlank())
		{
			logger.warn("processVersion null or blank");
			return Optional.empty();
		}
		else if (messageName == null || messageName.isBlank())
		{
			logger.warn("messageName null or blank");
			return Optional.empty();
		}

		try
		{
			Optional<ActivityDefinition> optAD = dao
					.readByOrganizationTypeUserRoleProcessUrlVersionMessageNameAndNotRetiredWithTransaction(connection,
							organizationType, user.getOrganizationType(), user.getRole(), processUrl, processVersion,
							messageName);

			if (optAD.isPresent())
				logger.debug(
						"ActivityDefinition with recipient-organization-type {}, requester-organization-type {}, user-role {}, process-url {}, process-version {} and message-name {} found",
						organizationType, user.getOrganizationType(), user.getRole(), processUrl, processVersion,
						messageName);
			else
				logger.warn(
						"ActivityDefinition with recipient-organization-type {}, requester-organization-type {}, user-role {}, process-url {}, process-version {} and message-name {} not found",
						organizationType, user.getOrganizationType(), user.getRole(), processUrl, processVersion,
						messageName);

			return optAD;
		}
		catch (SQLException e)
		{
			logger.warn("Error while reading ActivityDefinition", e);
			return Optional.empty();
		}
	}
}
