package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Practitioner-identifier", type = SearchParamType.TOKEN, documentation = "A practitioner's Identifier")
public class PractitionerIdentifier extends AbstractIdentifierParameter<Practitioner>
{
	public static final String RESOURCE_COLUMN = "practitioner";

	public PractitionerIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Practitioner))
			return false;

		Practitioner p = (Practitioner) resource;

		return identifierMatches(p.getIdentifier());
	}
}
