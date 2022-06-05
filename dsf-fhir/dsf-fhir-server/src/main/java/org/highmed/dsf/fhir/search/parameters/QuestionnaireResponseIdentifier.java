package org.highmed.dsf.fhir.search.parameters;

import java.util.List;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-identifier", type = SearchParamType.TOKEN, documentation = "The unique identifier for the questionnaire response")
public class QuestionnaireResponseIdentifier extends AbstractIdentifierParameter<QuestionnaireResponse>
{
	public static final String RESOURCE_COLUMN = "questionnaire_response";

	public QuestionnaireResponseIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof QuestionnaireResponse))
			return false;

		QuestionnaireResponse qr = (QuestionnaireResponse) resource;

		return identifierMatches(List.of(qr.getIdentifier()));
	}
}
