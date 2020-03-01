package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.NamingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingSystemAuthorizationRule extends AbstractAuthorizationRule<NamingSystem, NamingSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(NamingSystemAuthorizationRule.class);

	public NamingSystemAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(NamingSystem.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, NamingSystem newResource)
	{
		if (isLocalUser(user))
		{
			// TODO move check for name and authorization tag to validation layer
			if (newResource.hasName() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				try
				{
					Optional<NamingSystem> existing = getDao().readByNameWithTransaction(connection,
							newResource.getName());
					if (existing.isEmpty())
					{
						logger.info(
								"Create of NamingSystem authorized for local user '{}', NamingSystem with name does not exist",
								user.getName());
						return Optional.of("local user, NamingSystem with name does not exist yet");
					}
					else
					{
						logger.warn("Create of NamingSystem unauthorized, NamingSystem with name already exists");
						return Optional.empty();
					}
				}
				catch (SQLException e)
				{
					logger.warn(
							"Create of NamingSystem unauthorized, error while checking for existing NamingSystem with name",
							e);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of NamingSystem unauthorized, missing name or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, NamingSystem existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of NamingSystem authorized for local user '{}', NamingSystem has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized NamingSystem");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of NamingSystem authorized for remote user '{}', NamingSystem has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized NamingSystem");
		}
		else
		{
			logger.warn("Read of NamingSystem unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, NamingSystem oldResource,
			NamingSystem newResource)
	{
		if (isLocalUser(user))
		{
			// TODO move check for name and authorization tag to validation layer
			if (newResource.hasName() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				if (oldResource.getName().equals(newResource.getName()))
				{
					logger.info("Update of NamingSystem authorized for local user '{}', NamingSystem with name exist",
							user.getName());
					return Optional.of("local user, NamingSystem with name exist");
				}
				else
				{
					logger.warn("Update of NamingSystem unauthorized, new name not equal to existing resource");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of NamingSystem unauthorized, missing name or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, NamingSystem oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of NamingSystem authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of NamingSystem authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
