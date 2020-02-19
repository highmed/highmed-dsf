package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class ValueSetUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public ValueSetUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "value_set");
	}
}
