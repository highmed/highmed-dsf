package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class MeasureReportUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_TABLE = "current_measure_reports";
	private static final String RESOURCE_ID_COLUMN = "measure_report_id";

	public MeasureReportUserFilter(User user)
	{
		super(user, RESOURCE_TABLE, RESOURCE_ID_COLUMN);
	}

	public MeasureReportUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}
}
