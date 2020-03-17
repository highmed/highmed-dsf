package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-identifier", type = SearchParamType.TOKEN, documentation = "Any identifier for the organization (not the accreditation issuer's identifier)")
public class OrganizationIdentifier extends AbstractIdentifierParameter<Organization>
{
	public static final String RESOURCE_COLUMN = "organization";

	public OrganizationIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Organization))
			return false;

		Organization o = (Organization) resource;

		return identifierMatches(o.getIdentifier());
	}
}
