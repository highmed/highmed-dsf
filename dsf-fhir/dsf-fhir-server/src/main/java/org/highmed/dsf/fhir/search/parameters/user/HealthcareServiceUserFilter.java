package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class HealthcareServiceUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public HealthcareServiceUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "healthcare_service");
	}
}