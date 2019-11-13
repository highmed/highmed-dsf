package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class EndpointJsonFhirAdapter extends JsonFhirAdapter<Endpoint>
{
	public EndpointJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Endpoint.class);
	}
}
