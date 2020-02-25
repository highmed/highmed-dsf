package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceAuthorizationRule
		extends AbstractAuthorizationRule<HealthcareService, HealthcareServiceDao>
{
	public HealthcareServiceAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(HealthcareService.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, HealthcareService newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, HealthcareService existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, HealthcareService oldResource, HealthcareService newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, HealthcareService oldResource)
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