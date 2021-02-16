package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Measure-identifier", type = SearchParamType.TOKEN, documentation = "External identifier for the measure")
public class MeasureIdentifier extends AbstractIdentifierParameter<Measure>
{
	public static final String RESOURCE_COLUMN = "measure";

	public MeasureIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Measure))
			return false;

		Measure m = (Measure) resource;

		return identifierMatches(m.getIdentifier());
	}
}
