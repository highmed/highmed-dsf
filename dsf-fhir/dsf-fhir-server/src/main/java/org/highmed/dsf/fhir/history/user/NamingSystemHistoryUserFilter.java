package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.NamingSystemUserFilter;
import org.hl7.fhir.r4.model.NamingSystem;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class NamingSystemHistoryUserFilter extends NamingSystemUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = NamingSystem.class.getAnnotation(ResourceDef.class).name();

	public NamingSystemHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
