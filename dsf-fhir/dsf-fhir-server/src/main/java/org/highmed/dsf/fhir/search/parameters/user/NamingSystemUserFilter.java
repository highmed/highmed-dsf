package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class NamingSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public NamingSystemUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "naming_system");
	}
}
