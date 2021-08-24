package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.LibraryUserFilter;
import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class LibraryHistoryUserFilter extends LibraryUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = Library.class.getAnnotation(ResourceDef.class).name();

	public LibraryHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
