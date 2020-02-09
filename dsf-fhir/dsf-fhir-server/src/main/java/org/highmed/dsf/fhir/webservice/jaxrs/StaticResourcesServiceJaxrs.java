package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(StaticResourcesServiceJaxrs.PATH)
public class StaticResourcesServiceJaxrs extends AbstractServiceJaxrs<StaticResourcesService>
		implements StaticResourcesService
{
	public static final String PATH = "static";

	private static final Logger logger = LoggerFactory.getLogger(StaticResourcesServiceJaxrs.class);

	public StaticResourcesServiceJaxrs(StaticResourcesService delegate)
	{
		super(delegate);
	}

	@GET
	@Path("/{fileName}")
	@Override
	public Response getFile(@PathParam("fileName") String fileName, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getFile(fileName, uri, headers);
	}
}
