package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class ActivityDefinitionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "activity_definition";

	public ActivityDefinitionUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public ActivityDefinitionUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
