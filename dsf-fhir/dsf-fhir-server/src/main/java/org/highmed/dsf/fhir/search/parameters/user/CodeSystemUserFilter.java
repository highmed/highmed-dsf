package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class CodeSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "code_system";

	public CodeSystemUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public CodeSystemUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
