package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class NamingSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public NamingSystemUserFilter(User user)
	{
		super(user, "naming_system");
	}
}
