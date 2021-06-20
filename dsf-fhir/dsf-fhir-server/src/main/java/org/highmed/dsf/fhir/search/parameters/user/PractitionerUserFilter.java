package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class PractitionerUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "practitioner";

	public PractitionerUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public PractitionerUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
