package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/HealthcareService-identifier", type = SearchParamType.TOKEN, documentation = "External identifiers for this item")
public class HealthcareServiceIdentifier extends AbstractIdentifierParameter<HealthcareService>
{
	public static final String RESOURCE_COLUMN = "healthcare_service";

	public HealthcareServiceIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof HealthcareService))
			return false;

		HealthcareService h = (HealthcareService) resource;

		return identifierMatches(h.getIdentifier());
	}
}
