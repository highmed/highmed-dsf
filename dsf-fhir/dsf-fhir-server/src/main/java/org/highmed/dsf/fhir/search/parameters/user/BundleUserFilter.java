package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class BundleUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "bundle";

	public BundleUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public BundleUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
