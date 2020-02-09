package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PractitionerRoleHtmlFhirAdapter extends HtmlFhirAdapter<PractitionerRole>
{
	public PractitionerRoleHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, PractitionerRole.class);
	}
}
