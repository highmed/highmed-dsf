package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrganizationHtmlFhirAdapter extends HtmlFhirAdapter<Organization>
{
	public OrganizationHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Organization.class);
	}
}
