package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class OrganizationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public OrganizationUserFilter(User user)
	{
		super(user, "organization");
	}
}
