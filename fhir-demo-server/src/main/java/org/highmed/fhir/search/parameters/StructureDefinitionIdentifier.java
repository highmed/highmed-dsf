package org.highmed.fhir.search.parameters;

import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/StructureDefinition.​identifier", type = SearchParamType.TOKEN, documentation = "External identifier for the structure definition")
public class StructureDefinitionIdentifier extends AbstractIdentifierParameter<StructureDefinition>
{
	public static final String RESOURCE_COLUMN = "structure_definition";

	public StructureDefinitionIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	public StructureDefinitionIdentifier(String resourceColumn)
	{
		super(resourceColumn);
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof StructureDefinition))
			return false;

		StructureDefinition s = (StructureDefinition) resource;

		return identifierMatches(s.getIdentifier());
	}
}
