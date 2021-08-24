package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = CodeSystemDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-date", type = SearchParamType.DATE, documentation = "The code system publication date")
public class CodeSystemDate extends AbstractDateTimeParameter<CodeSystem>
{
	public static final String PARAMETER_NAME = "date";

	public CodeSystemDate()
	{
		super(PARAMETER_NAME, "(code_system->>'date')");
	}
}
