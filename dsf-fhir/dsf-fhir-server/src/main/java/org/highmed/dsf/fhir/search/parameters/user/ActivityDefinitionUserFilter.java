package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class ActivityDefinitionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public ActivityDefinitionUserFilter(User user)
	{
		super(user, "activity_definition");
	}
}
