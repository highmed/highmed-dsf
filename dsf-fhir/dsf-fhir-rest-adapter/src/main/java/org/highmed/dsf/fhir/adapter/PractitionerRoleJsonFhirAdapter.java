package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PractitionerRoleJsonFhirAdapter extends JsonFhirAdapter<PractitionerRole>
{
	public PractitionerRoleJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, PractitionerRole.class);
	}
}
