package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.OrganizationAffiliation;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OrganizationAffiliationJsonFhirAdapter extends JsonFhirAdapter<OrganizationAffiliation>
{
	public OrganizationAffiliationJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, OrganizationAffiliation.class);
	}
}
