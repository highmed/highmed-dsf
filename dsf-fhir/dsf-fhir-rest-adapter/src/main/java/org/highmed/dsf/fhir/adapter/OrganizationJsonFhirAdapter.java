package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrganizationJsonFhirAdapter extends JsonFhirAdapter<Organization>
{
	public OrganizationJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Organization.class);
	}
}
