package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PractitionerRoleAuthorizationRule
		extends AbstractMetaTagAuthorizationRule<PractitionerRole, PractitionerRoleDao>
{
	private static final Logger logger = LoggerFactory.getLogger(PractitionerRoleAuthorizationRule.class);

	public PractitionerRoleAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper)
	{
		super(PractitionerRole.class, daoProvider, serverBase, referenceResolver, organizationProvider,
				readAccessHelper);
	}

	protected Optional<String> newResourceOk(Connection connection, User user, PractitionerRole newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasOrganization())
		{
			if (!newResource.getOrganization().hasReference())
			{
				errors.add("PractitionerRole.organization.reference missing");
			}
		}
		else
		{
			errors.add("PractitionerRole.organization missing");
		}

		if (newResource.hasPractitioner())
		{
			if (!newResource.getPractitioner().hasReference())
			{
				errors.add("PractitionerRole.practitioner.reference missing");
			}
		}
		else
		{
			errors.add("PractitionerRole.practitioner missing");
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("PractitionerRole is missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, PractitionerRole newResource)
	{
		// no unique criteria for PractitionerRole
		return false;
	}

	@Override
	protected boolean modificationsOk(Connection connection, PractitionerRole oldResource, PractitionerRole newResource)
	{
		// no unique criteria for PractitionerRole
		return true;
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, PractitionerRole oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Expunge of PractitionerRole authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of PractitionerRole unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
