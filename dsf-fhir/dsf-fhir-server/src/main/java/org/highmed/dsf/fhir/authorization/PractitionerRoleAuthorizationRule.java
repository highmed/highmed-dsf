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
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleAuthorizationRule
		extends AbstractMetaTagAuthorizationRule<PractitionerRole, PractitionerRoleDao>
{
	public PractitionerRoleAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(PractitionerRole.class, daoProvider, serverBase, referenceResolver, organizationProvider,
				readAccessHelper, parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, PractitionerRole newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, PractitionerRole newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, PractitionerRole newResource)
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
}
