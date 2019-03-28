package org.highmed.fhir.search.parameters;

import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/CodeSystem.​identifier", type = SearchParamType.TOKEN, documentation = "External identifier for the code system")
public class CodeSystemIdentifier extends AbstractIdentifierParameter<CodeSystem>
{
	public static final String RESOURCE_COLUMN = "code_system";

	public CodeSystemIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof CodeSystem))
			return false;

		CodeSystem c = (CodeSystem) resource;

		return identifierMatches(c.getIdentifier());
	}
}
