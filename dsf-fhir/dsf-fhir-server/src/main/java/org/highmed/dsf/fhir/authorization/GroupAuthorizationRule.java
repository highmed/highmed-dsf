package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupAuthorizationRule extends AbstractAuthorizationRule<Group, GroupDao>
{
	private static final Logger logger = LoggerFactory.getLogger(GroupAuthorizationRule.class);

	public GroupAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(Group.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Group newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of Group authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of Group unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Group existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of Group authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else if (isRemoteUser(user))
		{
			if (researchStudyWithEnrollmentAndUsersOrganizationExists(connection, user, existingResource))
			{
				logger.info(
						"Read of Group authorized for remote user '{}', ResearchStudy with enrollment contains this Group and users organization part of ResearchStudy",
						user.getName());
				return Optional.of(
						"remote user, users organization part of ResearchStudy with enrollment contains this Group");
			}
			else
			{
				logger.warn("ResearchStudy.enrollment containing this Group and users Organization not found");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Read of Group unauthorized, not a local or remote user");
			return Optional.empty();
		}
	}

	private boolean researchStudyWithEnrollmentAndUsersOrganizationExists(Connection connection, User user,
			Group existingResource)
	{
		try
		{
			return daoProvider.getResearchStudyDao()
					.existsByEnrollmentIdAndOrganizationTypeAndOrganizationIdWithTransaction(connection,
							existingResource.getIdElement(), user.getOrganizationType(),
							user.getOrganization().getIdElement());
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for research studies", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Group oldResource, Group newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of Group authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of Group unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Group oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Group authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Group unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of Group authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
