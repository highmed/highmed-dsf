package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Measure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureAuthorizationRule extends AbstractAuthorizationRule<Measure, MeasureDao>
{
	private static final Logger logger = LoggerFactory.getLogger(MeasureAuthorizationRule.class);

	public MeasureAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(Measure.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Measure newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of Measure authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of Measure unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Measure existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Measure authorized for local user '{}', Measure has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized Measure");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of Measure authorized for remote user '{}', Measure has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized Measure");
		}
		else
		{
			logger.warn("Read of Measure unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Measure oldResource,
			Measure newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of Measure authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of Measure unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Measure oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Measure authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Measure unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of Measure authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of Measure authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
