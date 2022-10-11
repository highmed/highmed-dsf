package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class QuestionnaireResponseUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_TABLE = "current_questionnaire_responses";
	private static final String RESOURCE_ID_COLUMN = "questionnaire_response_id";

	public QuestionnaireResponseUserFilter(User user)
	{
		super(user, RESOURCE_TABLE, RESOURCE_ID_COLUMN);
	}

	public QuestionnaireResponseUserFilter(User user, String resourceTable, String resourceIdColumn)
	{
		super(user, resourceTable, resourceIdColumn);
	}
}
