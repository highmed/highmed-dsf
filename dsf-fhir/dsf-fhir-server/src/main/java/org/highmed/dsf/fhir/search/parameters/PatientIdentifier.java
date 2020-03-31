package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Patient-identifier", type = SearchParamType.TOKEN, documentation = "A patient identifier")
public class PatientIdentifier extends AbstractIdentifierParameter<Patient>
{
	public static final String RESOURCE_COLUMN = "patient";

	public PatientIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Patient))
			return false;

		Patient p = (Patient) resource;

		return identifierMatches(p.getIdentifier());
	}
}
