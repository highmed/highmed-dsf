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
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.NamingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingSystemAuthorizationRule extends AbstractAuthorizationRule<NamingSystem, NamingSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(NamingSystemAuthorizationRule.class);

	public NamingSystemAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(NamingSystem.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, NamingSystem newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
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
			else
			{
				logger.warn("Create of NamingSystem unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(NamingSystem newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasStatus())
		{
			if (!EnumSet.of(PublicationStatus.DRAFT, PublicationStatus.ACTIVE, PublicationStatus.RETIRED)
					.contains(newResource.getStatus()))
			{
				errors.add("NamingSystem.status not one of DRAFT, ACTIVE or RETIRED");
			}
		}
		else
		{
			errors.add("NamingSystem.status not defined");
		}

		if (!newResource.hasName())
		{
			errors.add("NamingSystem.name not defined");
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

	private boolean resourceExists(Connection connection, NamingSystem newResource)
	{
		try
		{
			return getDao().readByNameWithTransaction(connection, newResource.getName()).isPresent();
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for NamingSystem", e);
			return false;
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
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of NamingSystem authorized for local user '{}', name same as existing NamingSystem",
							user.getName());
					return Optional.of("local user; name same as existing NamingSystem");
				}
				else
				{
					logger.warn("Update of NamingSystem unauthorized, name changed ({} -> {})", oldResource.getName(),
							newResource.getName());
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of NamingSystem unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(NamingSystem oldResource, NamingSystem newResource)
	{
		return oldResource.getName().equals(newResource.getName());
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
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of NamingSystem authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of NamingSystem authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, NamingSystem oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Expunge of NamingSystem authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of NamingSystem unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
