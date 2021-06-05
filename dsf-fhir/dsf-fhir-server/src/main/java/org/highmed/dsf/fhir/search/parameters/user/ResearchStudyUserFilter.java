package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class ResearchStudyUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "research_study";

	public ResearchStudyUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public ResearchStudyUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
