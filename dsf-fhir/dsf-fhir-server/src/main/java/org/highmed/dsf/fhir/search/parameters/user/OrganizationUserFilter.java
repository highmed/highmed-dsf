package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class OrganizationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "organization";

	public OrganizationUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public OrganizationUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
