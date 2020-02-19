package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionAuthorizationRule extends AbstractAuthorizationRule<Subscription, SubscriptionDao>
{
	public SubscriptionAuthorizationRule(SubscriptionDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Subscription newResource)
	{
		// check subscription.channel.payload null or one of the supported mimetypes
		// check subscription.channel.type = websocket
		// check subscription.criteria is implemented as search query
		// check if subscription.channel.type = websocket, Task unique on subscription.criteria
		
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Subscription existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Subscription oldResource, Subscription newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Subscription oldResource)
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
