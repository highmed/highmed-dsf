package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class LocationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "location";

	public LocationUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public LocationUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
