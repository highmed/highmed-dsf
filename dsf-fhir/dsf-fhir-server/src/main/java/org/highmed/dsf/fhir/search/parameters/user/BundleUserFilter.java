package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class BundleUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public BundleUserFilter(User user)
	{
		super(user, "bundle");
	}
}
