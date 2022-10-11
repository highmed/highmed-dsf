package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Questionnaire;

@SearchParameterDefinition(name = QuestionnaireDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Questionnaire-date", type = SearchParamType.DATE, documentation = "The questionnaire publication date")
public class QuestionnaireDate extends AbstractDateTimeParameter<Questionnaire>
{
	public static final String PARAMETER_NAME = "date";

	public QuestionnaireDate()
	{
		super(PARAMETER_NAME, "questionnaire->>'date'");
	}
}
