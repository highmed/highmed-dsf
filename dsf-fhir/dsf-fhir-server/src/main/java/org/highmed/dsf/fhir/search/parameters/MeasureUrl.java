package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractUrlAndVersionParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = MeasureUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Measure-url", type = SearchParamType.URI, documentation = "The uri that identifies the measure")
public class MeasureUrl extends AbstractUrlAndVersionParameter<Measure>
{
	public static final String RESOURCE_COLUMN = "measure";

	public MeasureUrl()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof Measure;
	}
}
