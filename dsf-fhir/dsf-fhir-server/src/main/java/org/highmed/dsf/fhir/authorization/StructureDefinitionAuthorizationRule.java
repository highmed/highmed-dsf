package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<StructureDefinition, StructureDefinitionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionAuthorizationRule.class);

	public StructureDefinitionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(StructureDefinition.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, StructureDefinition newResource)
	{
		if (isLocalUser(user))
		{
			// TODO move check for url, version and authorization tag to validation layer
			if (newResource.hasUrl() && newResource.hasVersion() && hasLocalOrRemoteAuthorizationRole(newResource))
			{
				try
				{
					Optional<StructureDefinition> existing = getDao().readByUrlAndVersionWithTransaction(connection,
							newResource.getUrl(), newResource.getVersion());
					if (existing.isEmpty())
					{
						logger.info(
								"Create of StructureDefinition authorized for local user '{}', StructureDefinition with version and url does not exist",
								user.getName());
						return Optional.of("local user, StructureDefinition with version and url does not exist yet");
					}
					else
					{
						logger.warn(
								"Create of StructureDefinition unauthorized, StructureDefinition with url and version already exists");
						return Optional.empty();
					}
				}
				catch (SQLException e)
				{
					logger.warn(
							"Create of StructureDefinition unauthorized, error while checking for existing StructureDefinition with version and url",
							e);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of StructureDefinition unauthorized, missing url or version or authorization tag");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of StructureDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, StructureDefinition existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, StructureDefinition oldResource,
			StructureDefinition newResource)
	{
		// check logged in, check "local" user (local user only could be default)
		// check resource exists for given path id
		// check against existing profile (by path id), no update if profile has different URL, version or status,
		// status change via create
		// check against existing profile (by path id), no update if status ACTIVE or RETIRED

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, StructureDefinition oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
