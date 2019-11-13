package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

@Path(ConformanceServiceJaxrs.PATH)
@Produces({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML,
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class ConformanceServiceJaxrs implements ConformanceService
{
	public static final String PATH = "metadata";

	private static final Logger logger = LoggerFactory.getLogger(ConformanceServiceJaxrs.class);

	private final ConformanceService delegate;

	public ConformanceServiceJaxrs(ConformanceService delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public String getPath()
	{
		return PATH;
	}

	@GET
	@Override
	public Response getMetadata(@QueryParam("mode") String mode, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getMetadata(mode, uri, headers);
	}
}
