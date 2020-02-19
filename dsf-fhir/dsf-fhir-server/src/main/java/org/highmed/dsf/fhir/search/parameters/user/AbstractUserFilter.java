package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;

abstract class AbstractUserFilter implements SearchQueryUserFilter
{
	protected final OrganizationType organizationType;
	protected final User user;

	public AbstractUserFilter(OrganizationType organizationType, User user)
	{
		this.organizationType = organizationType;
		this.user = user;
	}
}
