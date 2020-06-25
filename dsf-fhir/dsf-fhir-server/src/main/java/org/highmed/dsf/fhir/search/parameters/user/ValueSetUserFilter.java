package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class ValueSetUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "value_set";

	public ValueSetUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public ValueSetUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
