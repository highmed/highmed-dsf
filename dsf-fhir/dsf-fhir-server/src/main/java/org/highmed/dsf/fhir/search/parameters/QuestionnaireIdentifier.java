package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Questionnaire-identifier", type = SearchParamType.TOKEN, documentation = "External identifier for the questionnaire")
public class QuestionnaireIdentifier extends AbstractIdentifierParameter<Questionnaire>
{
	public static final String RESOURCE_COLUMN = "questionnaire";

	public QuestionnaireIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Questionnaire))
			return false;

		Questionnaire q = (Questionnaire) resource;

		return identifierMatches(q.getIdentifier());
	}
}
