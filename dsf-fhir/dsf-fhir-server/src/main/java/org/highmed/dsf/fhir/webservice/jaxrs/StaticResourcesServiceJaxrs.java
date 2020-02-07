package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@Path(StaticResourcesServiceJaxrs.PATH)
public class StaticResourcesServiceJaxrs implements StaticResourcesService, InitializingBean
{
	public static final String PATH = "static";
	
	private static final Logger logger = LoggerFactory.getLogger(StaticResourcesServiceJaxrs.class);
	
	@Context
	private volatile HttpServletRequest httpRequest;

	protected final StaticResourcesService delegate;

	public StaticResourcesServiceJaxrs(StaticResourcesService delegate)
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


	@GET
	@Path("/{fileName}")
	@Override
	public Response getFile(@PathParam("fileName") String fileName, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());
		
		return delegate.getFile(fileName, uri, headers);
	}
}
