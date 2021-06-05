package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class PractitionerRoleUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static String RESOURCE_COLUMN = "practitioner_role";

	public PractitionerRoleUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public PractitionerRoleUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
