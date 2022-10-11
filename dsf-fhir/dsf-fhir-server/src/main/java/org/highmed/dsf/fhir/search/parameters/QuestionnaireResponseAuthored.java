package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

@SearchParameterDefinition(name = QuestionnaireResponseAuthored.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/QuestionnaireRespone-authored", type = SearchParamType.DATE, documentation = "When the questionnaire response was last changed")
public class QuestionnaireResponseAuthored extends AbstractDateTimeParameter<QuestionnaireResponse>
{
	public static final String PARAMETER_NAME = "authored";

	public QuestionnaireResponseAuthored()
	{
		super(PARAMETER_NAME, "questionnaire_response->>'authored'");
	}
}
