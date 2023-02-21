package org.highmed.dsf.fhir.webservice.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

@Path(ConformanceServiceJaxrs.PATH)
@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ MediaType.TEXT_HTML, Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON,
		Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
public class ConformanceServiceJaxrs extends AbstractServiceJaxrs<ConformanceService> implements ConformanceService
{
	public static final String PATH = "metadata";

	private static final Logger logger = LoggerFactory.getLogger(ConformanceServiceJaxrs.class);

	public ConformanceServiceJaxrs(ConformanceService delegate)
	{
		super(delegate);
	}

	@GET
	@Override
	public Response getMetadata(@QueryParam("mode") String mode, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getMetadata(mode, uri, headers);
	}
}
