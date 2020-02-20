package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.StructureDefinition;

public class StructureDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<StructureDefinition, StructureDefinitionDao>
{
	public StructureDefinitionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(StructureDefinition.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, StructureDefinition newResource)
	{
		// check logged in, check "local" user (local user only could be default)
		// check against existing profiles, no create if profile with same URL, version and status exists

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, StructureDefinition existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, StructureDefinition oldResource,
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
	public Optional<String> reasonDeleteAllowed(User user, StructureDefinition oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
