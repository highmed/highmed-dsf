package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class EndpointHtmlFhirAdapter extends HtmlFhirAdapter<Endpoint>
{
	public EndpointHtmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Endpoint.class);
	}
}
