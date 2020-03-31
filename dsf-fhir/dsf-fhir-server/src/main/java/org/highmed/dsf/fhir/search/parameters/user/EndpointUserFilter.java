package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class EndpointUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public EndpointUserFilter(User user)
	{
		super(user, "endpoint");
	}
}
