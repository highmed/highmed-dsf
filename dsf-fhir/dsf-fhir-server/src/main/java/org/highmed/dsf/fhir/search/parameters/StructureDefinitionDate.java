package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

@SearchParameterDefinition(name = StructureDefinitionDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-date", type = SearchParamType.DATE, documentation = "The structure definition publication date")
public class StructureDefinitionDate extends AbstractDateTimeParameter<StructureDefinition>
{
	public static final String PARAMETER_NAME = "date";

	public StructureDefinitionDate()
	{
		this("structure_definition");
	}

	public StructureDefinitionDate(String resourceColumn)
	{
		super(PARAMETER_NAME, resourceColumn + "->>'date'");
	}
}
