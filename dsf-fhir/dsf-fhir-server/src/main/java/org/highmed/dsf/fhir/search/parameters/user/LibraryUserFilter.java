package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class LibraryUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "library";

	public LibraryUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public LibraryUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
