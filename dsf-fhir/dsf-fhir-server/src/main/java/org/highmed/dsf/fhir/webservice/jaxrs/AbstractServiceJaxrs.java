package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.servlet.http.HttpServletRequest;
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

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.api.Constants;

@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
public abstract class AbstractServiceJaxrs<R extends Resource, S extends BasicService<R>>
		implements BasicService<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceJaxrs.class);

	@Context
	private volatile HttpServletRequest httpRequest;

	protected final S delegate;

	public AbstractServiceJaxrs(S delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		setUserProvider(new UserProvider(() -> httpRequest));
	}

	@Override
	public String getPath()
	{
		return delegate.getPath();
	}

	@Override
	public void setUserProvider(UserProvider provider)
	{
		delegate.setUserProvider(provider);
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

	@PUT
	@Override
	public Response update(R resource, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("PUT {}", uri.getRequestUri().toString());

		return delegate.update(resource, uri, headers);
	}

	@DELETE
	@Path("/{id}")
	@Override
	public Response delete(@PathParam("id") String id, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("DELETE {}", uri.getRequestUri().toString());

		return delegate.delete(id, uri, headers);
	}

	@DELETE
	@Override
	public Response delete(@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("DELETE {}", uri.getRequestUri().toString());

		return delegate.delete(uri, headers);
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
	public Response postValidateExisting(@PathParam("validate") String validatePath, @PathParam("id") String id,
			Parameters parameters, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return delegate.postValidateExisting(validatePath, id, parameters, uri, headers);
	}

	@GET
	@Path("/{id}/{validate : [$]validate(/)?}")
	@Override
	public Response getValidateExisting(@PathParam("validate") String validatePath, @PathParam("id") String id,
			@Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		return delegate.getValidateExisting(validatePath, id, uri, headers);
	}
}
