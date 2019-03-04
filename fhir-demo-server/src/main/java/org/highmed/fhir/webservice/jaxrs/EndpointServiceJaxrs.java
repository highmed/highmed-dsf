package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

@Path(EndpointServiceJaxrs.PATH)
public class EndpointServiceJaxrs extends AbstractServiceJaxrs<Endpoint, EndpointService>
		implements EndpointService
{
	public static final String PATH = "Endpoint";

	public EndpointServiceJaxrs(EndpointService delegate)
	{
		super(delegate);
	}

	@Override
	public String getPath()
	{
		return PATH;
	}
}
