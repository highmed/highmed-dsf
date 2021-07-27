package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class PatientUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_TABLE = "current_patients";
	private static final String RESOURCE_ID_COLUMN = "patient_id";

	public PatientUserFilter(User user)
	{
		super(user, RESOURCE_TABLE, RESOURCE_ID_COLUMN);
	}

	public PatientUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}
}
