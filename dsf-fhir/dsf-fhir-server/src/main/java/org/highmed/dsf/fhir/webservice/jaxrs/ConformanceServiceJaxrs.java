package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(ConformanceServiceJaxrs.PATH)
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
