package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeSystemAuthorizationRule extends AbstractAuthorizationRule<CodeSystem, CodeSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(CodeSystemAuthorizationRule.class);

	public CodeSystemAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(CodeSystem.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, CodeSystem newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of CodeSystem authorized for local user '{}', CodeSystem with version and url does not exist",
							user.getName());
					return Optional.of("local user, CodeSystem with version and url does not exist yet");
				}
				else
				{
					logger.warn("Create of CodeSystem unauthorized, CodeSystem with url and version already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of CodeSystem unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(CodeSystem newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasStatus())
		{
			if (!EnumSet.of(PublicationStatus.DRAFT, PublicationStatus.ACTIVE, PublicationStatus.RETIRED)
					.contains(newResource.getStatus()))
			{
				errors.add("CodeSystem.status not one of DRAFT, ACTIVE or RETIRED");
			}
		}
		else
		{
			errors.add("CodeSystem.status not defined");
		}

		if (!newResource.hasUrl())
		{
			errors.add("CodeSystem.url not defined");
		}
		if (!newResource.hasVersion())
		{
			errors.add("CodeSystem.version not defined");
		}

		if (!hasLocalOrRemoteAuthorizationRole(newResource))
		{
			errors.add("missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean resourceExists(Connection connection, CodeSystem newResource)
	{
		try
		{
			return getDao()
					.readByUrlAndVersionWithTransaction(connection, newResource.getUrl(), newResource.getVersion())
					.isPresent();
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for CodeSystem", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, CodeSystem existingResource)
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
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, CodeSystem oldResource,
			CodeSystem newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of CodeSystem authorized for local user '{}', url and version same as existing CodeSystem",
							user.getName());
					return Optional.of("local user; url and version same as existing CodeSystem");
				}
				else
				{
					logger.warn("Update of CodeSystem unauthorized, url or version changed ({} -> {}, {} -> {})",
							oldResource.getUrl(), newResource.getUrl(), oldResource.getVersion(),
							newResource.getVersion());
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of CodeSystem unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(CodeSystem oldResource, CodeSystem newResource)
	{
		return oldResource.getUrl().equals(newResource.getUrl())
				&& oldResource.getVersion().equals(newResource.getVersion());
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, CodeSystem oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of CodeSystem authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of CodeSystem unauthorized, not a local user");
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

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of CodeSystem authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, CodeSystem oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Expunge of CodeSystem authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of CodeSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
