package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EndpointHtmlFhirAdapter extends HtmlFhirAdapter<Endpoint>
{
	public EndpointHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Endpoint.class);
	}
}
