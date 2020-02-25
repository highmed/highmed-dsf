package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class ValueSetUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public ValueSetUserFilter(User user)
	{
		super(user, "value_set");
	}
}
