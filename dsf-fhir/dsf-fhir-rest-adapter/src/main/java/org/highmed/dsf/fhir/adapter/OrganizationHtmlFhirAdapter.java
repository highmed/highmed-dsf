package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OrganizationHtmlFhirAdapter extends HtmlFhirAdapter<Organization>
{
	public OrganizationHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Organization.class);
	}
}
