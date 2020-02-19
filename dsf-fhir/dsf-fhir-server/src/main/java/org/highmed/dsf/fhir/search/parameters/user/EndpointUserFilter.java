package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class EndpointUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public EndpointUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "endpoint");
	}
}
