package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class SubscriptionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "subscription";

	public SubscriptionUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public SubscriptionUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
