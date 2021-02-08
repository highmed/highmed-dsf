package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.StructureDefinitionService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(StructureDefinitionServiceJaxrs.PATH)
public class StructureDefinitionServiceJaxrs
		extends AbstractResourceServiceJaxrs<StructureDefinition, StructureDefinitionService>
		implements StructureDefinitionService
{
	public static final String PATH = "StructureDefinition";

	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionServiceJaxrs.class);

	public StructureDefinitionServiceJaxrs(StructureDefinitionService delegate)
	{
		super(delegate);
	}

	@POST
	@Path("/{snapshot : [$]snapshot(/)?}")
	@Override
	public Response postSnapshotNew(@PathParam("snapshot") String snapshotPath, Parameters parameters,
			@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.postSnapshotNew(snapshotPath, parameters, uri, headers);
	}

	@GET
	@Path("/{snapshot : [$]snapshot(/)?}")
	@Override
	public Response getSnapshotNew(@PathParam("snapshot") String snapshotPath, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getSnapshotNew(snapshotPath, uri, headers);
	}

	@POST
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	@Override
	public Response postSnapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.postSnapshotExisting(snapshotPath, id, uri, headers);
	}

	@GET
	@Path("/{id}/{snapshot : [$]snapshot(/)?}")
	@Override
	public Response getSnapshotExisting(@PathParam("snapshot") String snapshotPath, @PathParam("id") String id,
			@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getSnapshotExisting(snapshotPath, id, uri, headers);
	}
}
