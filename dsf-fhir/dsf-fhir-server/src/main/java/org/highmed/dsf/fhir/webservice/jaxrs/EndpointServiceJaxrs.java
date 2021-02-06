package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

@Path(EndpointServiceJaxrs.PATH)
public class EndpointServiceJaxrs extends AbstractResourceServiceJaxrs<Endpoint, EndpointService>
		implements EndpointService
{
	public static final String PATH = "Endpoint";

	public EndpointServiceJaxrs(EndpointService delegate)
	{
		super(delegate);
	}
}
