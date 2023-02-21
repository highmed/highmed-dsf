package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PractitionerJsonFhirAdapter extends JsonFhirAdapter<Practitioner>
{
	public PractitionerJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Practitioner.class);
	}
}
