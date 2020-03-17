package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractVersionParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

@SearchParameterDefinition(name = StructureDefinitionVersion.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/StructureDefinition-version", type = SearchParamType.TOKEN, documentation = "The business version of the structure definition")
public class StructureDefinitionVersion extends AbstractVersionParameter<StructureDefinition>
{
	public static final String RESOURCE_COLUMN = "structure_definition";

	public StructureDefinitionVersion()
	{
		this(RESOURCE_COLUMN);
	}

	public StructureDefinitionVersion(String resourceColumn)
	{
		super(resourceColumn);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof StructureDefinition;
	}
}
