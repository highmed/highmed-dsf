package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractUrlAndVersionParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

@SearchParameterDefinition(name = StructureDefinitionUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/StructureDefinition-url", type = SearchParamType.URI, documentation = "The uri that identifies the structure definition")
public class StructureDefinitionUrl extends AbstractUrlAndVersionParameter<StructureDefinition>
{
	public static final String RESOURCE_COLUMN = "structure_definition";

	public StructureDefinitionUrl()
	{
		this(RESOURCE_COLUMN);
	}

	public StructureDefinitionUrl(String resourceColumn)
	{
		super(resourceColumn);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof StructureDefinition;
	}
}
