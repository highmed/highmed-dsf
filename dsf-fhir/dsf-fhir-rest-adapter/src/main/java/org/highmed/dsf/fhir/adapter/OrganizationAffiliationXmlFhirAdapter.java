package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.OrganizationAffiliation;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrganizationAffiliationXmlFhirAdapter extends XmlFhirAdapter<OrganizationAffiliation>
{
	public OrganizationAffiliationXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, OrganizationAffiliation.class);
	}
}
