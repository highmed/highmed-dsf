package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.HealthcareServiceUserFilter;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class HealthcareServiceHistoryUserFilter extends HealthcareServiceUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = HealthcareService.class.getAnnotation(ResourceDef.class).name();

	public HealthcareServiceHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
