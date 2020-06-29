package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class EndpointUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "endpoint";

	public EndpointUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public EndpointUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
