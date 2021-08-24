package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.ValueSet;

@SearchParameterDefinition(name = ValueSetDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-date", type = SearchParamType.DATE, documentation = "The value set publication date")
public class ValueSetDate extends AbstractDateTimeParameter<ValueSet>
{
	public static final String PARAMETER_NAME = "date";

	public ValueSetDate()
	{
		super(PARAMETER_NAME, "value_set->>'date'");
	}
}
