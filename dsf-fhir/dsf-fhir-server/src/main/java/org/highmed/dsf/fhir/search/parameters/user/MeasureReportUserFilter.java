package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class MeasureReportUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "measure_report";

	public MeasureReportUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public MeasureReportUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
