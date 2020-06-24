package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAuthorizationRule extends AbstractAuthorizationRule<Bundle, BundleDao>
{
	private static final Logger logger = LoggerFactory.getLogger(BundleAuthorizationRule.class);

	public BundleAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(Bundle.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Bundle newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				logger.info("Create of Bundle authorized for local user '{}'", user.getName());
				return Optional.of("local user");
			}
			else
			{
				logger.warn("Create of Bundle unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of Bundle unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Bundle newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (!hasLocalOrRemoteAuthorizationRole(newResource))
		{
			errors.add("missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Bundle existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of Bundle authorized for local user '{}', Bundle has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized Bundle");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of Bundle authorized for remote user '{}', Bundle has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized Bundle");
		}
		else
		{
			logger.warn("Read of Bundle unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Bundle oldResource,
			Bundle newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				logger.info("Update of Bundle authorized for local user '{}'", user.getName());
				return Optional.of("local user");
			}
			else
			{
				logger.warn("Update of Bundle unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of Bundle unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Bundle oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Bundle authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Bundle unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of Bundle authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of Bundle authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
