package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OrganizationJsonFhirAdapter extends JsonFhirAdapter<Organization>
{
	public OrganizationJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Organization.class);
	}
}
