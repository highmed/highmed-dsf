package org.highmed.dsf.fhir.webservice.jaxrs;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@Path(StatusServiceJaxrs.PATH)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class StatusServiceJaxrs implements StatusService, InitializingBean
{
	public static final String PATH = "status";

	private static final Logger logger = LoggerFactory.getLogger(StatusServiceJaxrs.class);

	private final StatusService delegate;

	public StatusServiceJaxrs(StatusService delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(delegate, "delegate");
	}

	@Override
	public String getPath()
	{
		return delegate.getPath();
	}

	@GET
	@Override
	public Response status(@Context UriInfo uri, @Context HttpHeaders headers, @Context HttpServletRequest request)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.status(uri, headers, request);
	}
}
