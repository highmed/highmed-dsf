package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;

import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

public class EndpointServiceSecure extends AbstractServiceSecure<Endpoint, EndpointService> implements EndpointService
{
	public EndpointServiceSecure(EndpointService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	protected Optional<String> reasonCreateNotAllowed(Endpoint resource)
	{
		// TODO validate unique on Endpoint.address

		return super.reasonCreateNotAllowed(resource);
	}

	@Override
	protected Optional<String> reasonUpdateNotAllowed(String id, Endpoint resource)
	{
		// TODO validate unique on Endpoint.address

		return super.reasonUpdateNotAllowed(id, resource);
	}

	@Override
	protected Optional<String> reasonUpdateNotAllowed(Endpoint resource, UriInfo uri)
	{
		// TODO validate unique on Endpoint.address

		return super.reasonUpdateNotAllowed(resource, uri);
	}
}
