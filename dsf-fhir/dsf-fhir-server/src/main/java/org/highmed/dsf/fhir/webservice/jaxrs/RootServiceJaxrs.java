package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(RootServiceJaxrs.PATH)
public class RootServiceJaxrs extends AbstractServiceJaxrs<RootService> implements RootService
{
	public static final String PATH = "";

	private static final Logger logger = LoggerFactory.getLogger(RootServiceJaxrs.class);

	public RootServiceJaxrs(RootService delegate)
	{
		super(delegate);
	}

	@GET
	@Override
	public Response root(@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.root(uri, headers);
	}

	@POST
	@Override
	public Response handleBundle(Bundle bundle, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.handleBundle(bundle, uri, headers);
	}
}
