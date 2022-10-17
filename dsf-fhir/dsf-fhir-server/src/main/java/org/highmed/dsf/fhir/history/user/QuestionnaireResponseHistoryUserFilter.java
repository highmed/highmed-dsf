package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.QuestionnaireResponseUserFilter;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class QuestionnaireResponseHistoryUserFilter extends QuestionnaireResponseUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = QuestionnaireResponse.class.getAnnotation(ResourceDef.class).name();

	public QuestionnaireResponseHistoryUserFilter(User user)
	{
		super(user);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
