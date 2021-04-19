package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.MeasureReportDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.MeasureReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureReportAuthorizationRule extends AbstractAuthorizationRule<MeasureReport, MeasureReportDao>
{
	private static final Logger logger = LoggerFactory.getLogger(MeasureReportAuthorizationRule.class);

	public MeasureReportAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(MeasureReport.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, MeasureReport newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of MeasureReport authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of MeasureReport unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, MeasureReport existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of MeasureReport authorized for local user '{}', MeasureReport has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized MeasureReport");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of MeasureReport authorized for remote user '{}', MeasureReport has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized MeasureReport");
		}
		else
		{
			logger.warn("Read of MeasureReport unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, MeasureReport oldResource,
			MeasureReport newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of MeasureReport authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of MeasureReport unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, MeasureReport oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of MeasureReport authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of MeasureReport unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of MeasureReport authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of MeasureReport authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, MeasureReport oldResource) {
		if (isLocalUser(user))
		{
			logger.info("Expunge of MeasureReport authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of MeasureReport unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
