package org.highmed.dsf.fhir.authorization;

import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueSetAuthorizationRule extends AbstractAuthorizationRule<ValueSet, ValueSetDao>
{
	private static final Logger logger = LoggerFactory.getLogger(ValueSetAuthorizationRule.class);

	public ValueSetAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver)
	{
		super(ValueSet.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, ValueSet newResource)
	{
		if (isLocalUser(user))
		{
			// TODO move check for url, version, authorization tag, to validation layer
			if (newResource.hasUrl() && newResource.hasVersion() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				try
				{
					Optional<ValueSet> existing = getDao().readByUrlAndVersion(newResource.getUrl(),
							newResource.getVersion());
					if (existing.isEmpty())
					{
						logger.info(
								"Create of ValueSet authorized for local user '{}', ValueSet with version and url does not exist",
								user.getName());
						return Optional.of("local user, ValueSet with version and url does not exist yet");
					}
					else
					{
						logger.warn("Create of ValueSet unauthorized, ValueSet wirh url and version already exists");
						return Optional.empty();
					}
				}
				catch (SQLException e)
				{
					logger.warn(
							"Create of ValueSet unauthorized, error while checking for existing ValueSet with version and url",
							e);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of ValueSet unauthorized, missing url or version or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of ValueSet unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, ValueSet existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of ValueSet authorized for local user '{}', ValueSet has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized ValueSet");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of ValueSet authorized for remote user '{}', ValueSet has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized ValueSet");
		}
		else
		{
			logger.warn("Read of ValueSet unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, ValueSet oldResource, ValueSet newResource)
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
							"Update of ValueSet authorized for local user '{}', version and url same as existing one",
							user.getName());
					return Optional.of("local user, ValueSet with version and url does not exist yet");
				}
				else
				{
					logger.warn("Update of ValueSet unauthorized, new url or version not equal to existing resource");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of ValueSet unauthorized, missing url or version or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of ValueSet unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, ValueSet oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of new ValueSet authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Update of ValueSet unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of ValueSet authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
