package org.highmed.dsf.fhir.authorization;

import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.hl7.fhir.r4.model.CodeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeSystemAuthorizationRule extends AbstractAuthorizationRule<CodeSystem, CodeSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(CodeSystemAuthorizationRule.class);

	public CodeSystemAuthorizationRule(CodeSystemDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, CodeSystem newResource)
	{
		if (isLocalUser(user))
		{
			if (newResource.hasUrl() && newResource.hasVersion() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				try
				{
					// TODO move check for url, version, authorization tag, to validation layer
					Optional<CodeSystem> existing = dao.readByUrlAndVersion(newResource.getUrl(),
							newResource.getVersion());
					if (existing.isEmpty())
					{
						logger.info(
								"Create of CodeSystem authorized for local user '{}', CodeSystem with version and url does not exist",
								user.getName());
						return Optional.of("local user, CodeSystem with version and url does not exist yet");
					}
					else
					{
						logger.warn(
								"Create of CodeSystem unauthorized, CodeSystem wirh url and version already exists");
						return Optional.empty();
					}
				}
				catch (SQLException e)
				{
					logger.warn(
							"Create of CodeSystem unauthorized, error while checking for existing CodeSystem with version and url",
							e);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of CodeSystem unauthorized, missing url or version or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, CodeSystem existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of CodeSystem authorized for local user '{}', CodeSystem has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized CodeSystem");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of CodeSystem authorized for remote user '{}', CodeSystem has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized CodeSystem");
		}
		else
		{
			logger.warn("Read of CodeSystem unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, CodeSystem oldResource, CodeSystem newResource)
	{
		if (isLocalUser(user))
		{
			// TODO move check for url, version, authorization tag, to validation layer
			if (newResource.hasUrl() && newResource.hasVersion() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				if (oldResource.getUrl().equals(newResource.getUrl())
						&& oldResource.getVersion().equals(newResource.getVersion()))
				{
					logger.info(
							"Update of CodeSystem authorized for local user '{}', version and url same as existing one",
							user.getName());
					return Optional.of("local user, CodeSystem with version and url does not exist yet");
				}
				else
				{
					logger.warn("Update of CodeSystem unauthorized, new url or version not equal to existing resource");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of CodeSystem unauthorized, missing url or version or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, CodeSystem oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of new CodeSystem authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Update of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of CodeSystem authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
