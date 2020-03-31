package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/ResearchStudy-identifier", type = SearchParamType.TOKEN, documentation = "Business Identifier for study")
public class ResearchStudyIdentifier extends AbstractIdentifierParameter<ResearchStudy>
{
	public static final String RESOURCE_COLUMN = "research_study";

	public ResearchStudyIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof ResearchStudy))
			return false;

		ResearchStudy r = (ResearchStudy) resource;

		return identifierMatches(r.getIdentifier());
	}
}
