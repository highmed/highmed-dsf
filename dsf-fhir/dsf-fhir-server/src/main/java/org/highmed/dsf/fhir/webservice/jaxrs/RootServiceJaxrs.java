package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

@Path(RootServiceJaxrs.PATH)
@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ MediaType.TEXT_HTML, Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON,
		Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
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

	@GET
	@Path("/_history")
	@Override
	public Response history(@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.history(uri, headers);
	}

	@POST
	@Override
	public Response handleBundle(Bundle bundle, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.handleBundle(bundle, uri, headers);
	}
}
