package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.search.SearchQueryUserFilter;

public interface HistoryUserFilter extends SearchQueryUserFilter
{
	String RESOURCE_ID_COLUMN = "id";
	String RESOURCE_COLUMN = "resource";
	String RESOURCE_TABLE = "history";

	static String getFilterQuery(String resourceType, String filterQuery)
	{
		if (filterQuery == null || filterQuery.isBlank())
			return "(type = '" + resourceType + "')";
		else
			return "(type = '" + resourceType + "' AND " + filterQuery + ")";
	}

	default boolean isDefined()
	{
		String filterQuery = getFilterQuery();
		return filterQuery != null && !filterQuery.isBlank();
	}
}
