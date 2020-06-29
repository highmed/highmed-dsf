package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class NamingSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "naming_system";

	public NamingSystemUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public NamingSystemUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
