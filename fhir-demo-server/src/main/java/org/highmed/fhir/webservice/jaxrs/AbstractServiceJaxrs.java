package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
public class AbstractServiceJaxrs<R extends DomainResource, S extends BasicService<R>> implements BasicService<R>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceJaxrs.class);

	protected final S delegate;

	public AbstractServiceJaxrs(S delegate)
	{
		this.delegate = delegate;
	}

	@POST
	@Override
	public Response create(R resource, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.create(resource, uri, headers);
	}

	@GET
	@Path("/{id}")
	@Override
	public Response read(@PathParam("id") String id, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.read(id, uri, headers);
	}

	@GET
	@Path("/{id}/_history/{version}")
	@Override
	public Response vread(@PathParam("id") String id, @PathParam("version") long version, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.vread(id, version, uri, headers);
	}

	@PUT
	@Path("/{id}")
	@Override
	public Response update(@PathParam("id") String id, R resource, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("PUT {}", uri.getRequestUri().toString());

		return delegate.update(id, resource, uri, headers);
	}

	@DELETE
	@Path("/{id}")
	@Override
	public Response delete(@PathParam("id") String id, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("DELETE {}", uri.getRequestUri().toString());

		return delegate.delete(id, uri, headers);
	}

	@GET
	@Override
	public Response search(@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.search(uri, headers);
	}

	@POST
	@Path("/{validate : [$]validate(/)?}")
	@Override
	public Response postValidateNew(@PathParam("validate") String validate, Parameters parameters, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@GET
	@Path("/{validate : [$]validate(/)?}")
	@Override
	public Response getValidateNew(@PathParam("validate") String validate, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getValidateNew(validate, uri, headers);
	}

	@POST
	@Path("/{id}/{validate : [$]validate(/)?}")
	@Override
	public Response postValidateExisting(@PathParam("validate") String validate, Parameters parameters,
			@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.postValidateExisting(validate, parameters, uri, headers);
	}

	@GET
	@Path("/{id}/{validate : [$]validate(/)?}")
	@Override
	public Response getValidateExisting(@PathParam("validate") String validate, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getValidateExisting(validate, uri, headers);
	}
}
