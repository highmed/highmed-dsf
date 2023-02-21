package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PractitionerRoleJsonFhirAdapter extends JsonFhirAdapter<PractitionerRole>
{
	public PractitionerRoleJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, PractitionerRole.class);
	}
}
