package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class PractitionerRoleUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_TABLE = "current_practitioner_roles";
	private static String RESOURCE_ID_COLUMN = "practitioner_role_id";

	public PractitionerRoleUserFilter(User user)
	{
		super(user, RESOURCE_TABLE, RESOURCE_ID_COLUMN);
	}

	public PractitionerRoleUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}
}
