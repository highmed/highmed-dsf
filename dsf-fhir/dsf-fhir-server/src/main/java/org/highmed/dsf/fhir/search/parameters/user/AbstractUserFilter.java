package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;

abstract class AbstractUserFilter implements SearchQueryUserFilter
{
	protected final User user;
	protected final String resourceTable;
	protected final String resourceIdColumn;

	public AbstractUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		this.user = user;
		this.resourceTable = resourceTable;
		this.resourceIdColumn = resourceIdColumn;
	}
}
