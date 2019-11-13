package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OrganizationXmlFhirAdapter extends XmlFhirAdapter<Organization>
{
	public OrganizationXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Organization.class);
	}
}
