package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.NamingSystem;

@SearchParameterDefinition(name = NamingSystemDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-date", type = SearchParamType.DATE, documentation = "The naming system publication date")
public class NamingSystemDate extends AbstractDateTimeParameter<NamingSystem>
{
	public static final String PARAMETER_NAME = "date";

	public NamingSystemDate()
	{
		super(PARAMETER_NAME, "naming_system->>'date'");
	}
}
