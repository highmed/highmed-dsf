package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointAuthorizationRule extends AbstractAuthorizationRule<Endpoint, EndpointDao>
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointAuthorizationRule.class);

	public EndpointAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver)
	{
		super(Endpoint.class, daoProvider, serverBase, referenceResolver);
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
		logger.info("Search of Endpoint authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
