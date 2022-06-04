package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Questionnaire;

@SearchParameterDefinition(name = QuestionnaireStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Questionnaire-status", type = SearchParamType.TOKEN, documentation = "The current status of the questionnaire")
public class QuestionnaireStatus extends AbstractStatusParameter<Questionnaire>
{
	public QuestionnaireStatus()
	{
		super("questionnaire", Questionnaire.class);
	}
}
