package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Measure;

@SearchParameterDefinition(name = MeasureStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Measure-status", type = SearchParamType.TOKEN, documentation = "The current status of the measure")
public class MeasureStatus extends AbstractStatusParameter<Measure>
{
	public MeasureStatus()
	{
		super("measure", Measure.class);
	}
}
