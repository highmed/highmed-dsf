package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.hl7.fhir.r4.model.Endpoint;

public class EndpointAuthorizationRule extends AbstractAuthorizationRule<Endpoint, EndpointDao>
{
	public EndpointAuthorizationRule(EndpointDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Endpoint newResource)
	{
		// TODO validate unique on Endpoint.address
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Endpoint existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Endpoint oldResource, Endpoint newResource)
	{
		// TODO validate unique on Endpoint.address
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Endpoint oldResource)
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
