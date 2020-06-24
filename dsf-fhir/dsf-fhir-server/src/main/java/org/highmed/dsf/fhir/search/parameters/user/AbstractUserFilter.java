package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;

abstract class AbstractUserFilter implements SearchQueryUserFilter
{
	protected final User user;
	protected final String resourceColumn;

	public AbstractUserFilter(User user, String resourceColumn)
	{
		this.user = user;
		this.resourceColumn = resourceColumn;
	}
}
