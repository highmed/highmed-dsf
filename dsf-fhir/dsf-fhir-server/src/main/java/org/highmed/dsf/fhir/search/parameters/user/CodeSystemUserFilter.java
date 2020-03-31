package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class CodeSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public CodeSystemUserFilter(User user)
	{
		super(user, "code_system");
	}
}
