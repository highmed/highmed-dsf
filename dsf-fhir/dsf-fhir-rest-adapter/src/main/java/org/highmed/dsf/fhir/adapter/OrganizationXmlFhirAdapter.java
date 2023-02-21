package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrganizationXmlFhirAdapter extends XmlFhirAdapter<Organization>
{
	public OrganizationXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Organization.class);
	}
}
