package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class GroupUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "group";

	public GroupUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public GroupUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
