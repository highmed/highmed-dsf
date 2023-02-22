package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.NamingSystem;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NamingSystemHtmlFhirAdapter extends HtmlFhirAdapter<NamingSystem>
{
	public NamingSystemHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, NamingSystem.class);
	}
}
