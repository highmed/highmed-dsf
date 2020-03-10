package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.HealthcareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthcareServiceAuthorizationRule
		extends AbstractAuthorizationRule<HealthcareService, HealthcareServiceDao>
{
	private static final Logger logger = LoggerFactory.getLogger(HealthcareServiceAuthorizationRule.class);

	public HealthcareServiceAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(HealthcareService.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, HealthcareService newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of HealthcareService authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of HealthcareService unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, HealthcareService existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of HealthcareService authorized for local user '{}', HealthcareService has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized HealthcareService");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of HealthcareService authorized for remote user '{}', HealthcareService has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized HealthcareService");
		}
		else
		{
			logger.warn(
					"Read of HealthcareService unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, HealthcareService oldResource,
			HealthcareService newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of HealthcareService authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of HealthcareService unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, HealthcareService oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of HealthcareService authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of HealthcareService unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of HealthcareService authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
