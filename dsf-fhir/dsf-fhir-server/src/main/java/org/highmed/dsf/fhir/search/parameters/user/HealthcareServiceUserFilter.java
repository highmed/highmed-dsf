package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class HealthcareServiceUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "healthcare_service";

	public HealthcareServiceUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public HealthcareServiceUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
