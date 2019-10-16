package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PractitionerJsonFhirAdapter extends JsonFhirAdapter<Practitioner>
{
	public PractitionerJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Practitioner.class);
	}
}
