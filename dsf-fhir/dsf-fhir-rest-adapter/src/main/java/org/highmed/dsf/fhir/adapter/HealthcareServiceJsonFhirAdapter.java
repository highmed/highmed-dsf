package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HealthcareServiceJsonFhirAdapter extends JsonFhirAdapter<HealthcareService>
{
	public HealthcareServiceJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, HealthcareService.class);
	}
}
