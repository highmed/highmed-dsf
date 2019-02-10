package org.highmed.fhir.werbservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

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
	@Produces({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW })
	public Response find(@PathParam("id") String id)
	{
		logger.trace("GET '{}/{}'", path, id);

		return Response.ok().build();
	}

	@POST
	@Consumes({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_XML_NEW })
	public Response create(D resource)
	{
		logger.trace("POST '{}'", path);

		logger.debug("IdElement: {}, Id: {}, IdBase: {}", resource.getIdElement(), resource.getId(),
				resource.getIdBase());

		return Response.ok().build();
	}
}
