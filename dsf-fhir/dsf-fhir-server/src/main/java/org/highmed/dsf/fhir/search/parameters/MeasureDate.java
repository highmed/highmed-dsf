package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Measure;

@SearchParameterDefinition(name = MeasureDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Measure-date", type = SearchParamType.DATE, documentation = "The measure publication date")
public class MeasureDate extends AbstractDateTimeParameter<Measure>
{
	public static final String PARAMETER_NAME = "date";

	public MeasureDate()
	{
		super(PARAMETER_NAME, "measure->>'date'");
	}
}
