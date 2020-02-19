package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class OrganizationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public OrganizationUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "organization");
	}
}
