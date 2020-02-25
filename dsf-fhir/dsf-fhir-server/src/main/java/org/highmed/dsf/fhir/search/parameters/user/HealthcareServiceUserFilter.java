package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class HealthcareServiceUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public HealthcareServiceUserFilter(User user)
	{
		super(user, "healthcare_service");
	}
}
