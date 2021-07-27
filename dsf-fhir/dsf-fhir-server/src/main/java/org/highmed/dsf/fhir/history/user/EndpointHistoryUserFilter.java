package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.EndpointUserFilter;
import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EndpointHistoryUserFilter extends EndpointUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = Endpoint.class.getAnnotation(ResourceDef.class).name();

	public EndpointHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
