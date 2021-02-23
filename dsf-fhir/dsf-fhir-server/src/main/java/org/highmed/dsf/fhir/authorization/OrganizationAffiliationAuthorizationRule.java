package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationAffiliationAuthorizationRule
		extends AbstractAuthorizationRule<OrganizationAffiliation, OrganizationAffiliationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAffiliationAuthorizationRule.class);

	public OrganizationAffiliationAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(OrganizationAffiliation.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, OrganizationAffiliation newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of OrganizationAffiliation authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of OrganizationAffiliation unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user,
			OrganizationAffiliation existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of OrganizationAffiliation authorized for local user '{}', OrganizationAffiliation has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized OrganizationAffiliation");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of OrganizationAffiliation authorized for remote user '{}', OrganizationAffiliation has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized OrganizationAffiliation");
		}
		else
		{
			logger.warn(
					"Read of OrganizationAffiliation unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, OrganizationAffiliation oldResource,
			OrganizationAffiliation newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of OrganizationAffiliation authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of OrganizationAffiliation unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, OrganizationAffiliation oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of OrganizationAffiliation authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of OrganizationAffiliation unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of OrganizationAffiliation authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of OrganizationAffiliation authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
