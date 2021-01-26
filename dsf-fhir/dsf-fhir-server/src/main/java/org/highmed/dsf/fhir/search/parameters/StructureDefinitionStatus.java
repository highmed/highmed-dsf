package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

@SearchParameterDefinition(name = StructureDefinitionStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/StructureDefinition-status", type = SearchParamType.TOKEN, documentation = "The current status of the structure definition")
public class StructureDefinitionStatus extends AbstractStatusParameter<StructureDefinition>
{
	public static final String RESOURCE_COLUMN = "structure_definition";

	public StructureDefinitionStatus()
	{
		this(RESOURCE_COLUMN);
	}

	public StructureDefinitionStatus(String resourceColumn)
	{
		super(resourceColumn, StructureDefinition.class);
	}
}
