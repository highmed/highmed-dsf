package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EndpointXmlFhirAdapter extends XmlFhirAdapter<Endpoint>
{
	public EndpointXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Endpoint.class);
	}
}
