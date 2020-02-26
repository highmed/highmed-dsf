package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationAuthorizationRule extends AbstractAuthorizationRule<Organization, OrganizationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAuthorizationRule.class);

	public OrganizationAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(Organization.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Organization newResource)
	{
		// check organization not existing if contains identifier with identifier.system (or extension)
		// http://highmed.org/fhir/NamingSystem/certificate-thumbprint-hex with same identifier.value
		// no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Organization existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Organization authorized for local user '{}', Organization has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized Organization");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Organization authorized for remote user '{}', Organization has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized Organization");
		}
		else
		{
			logger.warn("Read of Organization unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Organization oldResource, Organization newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Organization oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of Organization authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
