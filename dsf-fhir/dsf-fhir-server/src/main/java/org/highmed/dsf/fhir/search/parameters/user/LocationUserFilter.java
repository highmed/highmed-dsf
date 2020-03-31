package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class LocationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public LocationUserFilter(User user)
	{
		super(user, "location");
	}
}
