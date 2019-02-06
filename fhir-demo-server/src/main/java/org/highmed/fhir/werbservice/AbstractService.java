package org.highmed.fhir.werbservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractService<D extends DomainResource>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	private final String path;

	public AbstractService(String path)
	{
		this.path = path;
	}

	@GET
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response find(@PathParam("id") String id)
	{
		logger.trace("GET '{}/{}'", path, id);

		return Response.ok().build();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response create(D resource)
	{
		logger.trace("POST '{}'", path);

		return Response.ok().build();
	}
}
