package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class HealthcareServiceUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_TABLE = "current_healthcare_services";
	private static final String RESOURCE_ID_COLUMN = "healthcare_service_id";

	public HealthcareServiceUserFilter(User user)
	{
		super(user, RESOURCE_TABLE, RESOURCE_ID_COLUMN);
	}

	public HealthcareServiceUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}
}
