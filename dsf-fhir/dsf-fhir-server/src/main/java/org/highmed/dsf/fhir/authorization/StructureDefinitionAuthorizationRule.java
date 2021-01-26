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
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<StructureDefinition, StructureDefinitionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionAuthorizationRule.class);

	public StructureDefinitionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(StructureDefinition.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, StructureDefinition newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
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
			else
			{
				logger.warn("Create of StructureDefinition unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of StructureDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(StructureDefinition newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasStatus())
		{
			if (!EnumSet.of(PublicationStatus.DRAFT, PublicationStatus.ACTIVE, PublicationStatus.RETIRED)
					.contains(newResource.getStatus()))
			{
				errors.add("StructureDefinition.status not one of DRAFT, ACTIVE or RETIRED");
			}
		}
		else
		{
			errors.add("StructureDefinition.status not defined");
		}

		if (!newResource.hasUrl())
		{
			errors.add("StructureDefinition.url not defined");
		}
		if (!newResource.hasVersion())
		{
			errors.add("StructureDefinition.version not defined");
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

	private boolean resourceExists(Connection connection, StructureDefinition newResource)
	{
		try
		{
			return getDao()
					.readByUrlAndVersionWithTransaction(connection, newResource.getUrl(), newResource.getVersion())
					.map(s -> true).orElse(false);
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for StructureDefinition", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, StructureDefinition existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of StructureDefinition authorized for local user '{}', StructureDefinition has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized StructureDefinition");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of StructureDefinition authorized for remote user '{}', StructureDefinition has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized StructureDefinition");
		}
		else
		{
			logger.warn(
					"Read of StructureDefinition unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, StructureDefinition oldResource,
			StructureDefinition newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of StructureDefinition authorized for local user '{}', url and version same as existing StructureDefinition",
							user.getName());
					return Optional.of("local user; url and version same as existing StructureDefinition");
				}
				else
				{
					logger.warn(
							"Update of StructureDefinition unauthorized, url or version changed ({} -> {}, {} -> {})",
							oldResource.getUrl(), newResource.getUrl(), oldResource.getVersion(),
							newResource.getVersion());
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of StructureDefinition unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of StructureDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(StructureDefinition oldResource, StructureDefinition newResource)
	{
		return oldResource.getUrl().equals(newResource.getUrl())
				&& oldResource.getVersion().equals(newResource.getVersion());
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, StructureDefinition oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of StructureDefinition authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of StructureDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of StructureDefinition authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of StructureDefinition authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
