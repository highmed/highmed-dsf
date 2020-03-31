package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class SubscriptionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public SubscriptionUserFilter(User user)
	{
		super(user, "subscription");
	}
}
