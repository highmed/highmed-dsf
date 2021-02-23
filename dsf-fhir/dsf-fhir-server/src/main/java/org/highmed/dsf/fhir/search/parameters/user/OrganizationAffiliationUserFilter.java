package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class OrganizationAffiliationUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "organization_affiliation";

	public OrganizationAffiliationUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public OrganizationAffiliationUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
