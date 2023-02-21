package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Parameters;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ParametersJsonFhirAdapter extends JsonFhirAdapter<Parameters>
{
	public ParametersJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Parameters.class);
	}
}
