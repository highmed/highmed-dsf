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
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.NamingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingSystemAuthorizationRule extends AbstractMetaTagAuthorizationRule<NamingSystem, NamingSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(NamingSystemAuthorizationRule.class);

	public NamingSystemAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper)
	{
		super(NamingSystem.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper);
	}

	protected Optional<String> newResourceOk(Connection connection, User user, NamingSystem newResource)
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

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("NamingSystem is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	protected boolean resourceExists(Connection connection, NamingSystem newResource)
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
	protected boolean modificationsOk(Connection connection, NamingSystem oldResource, NamingSystem newResource)
	{
		return oldResource.getName().equals(newResource.getName());
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
