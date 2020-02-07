package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticResourcesServiceSecure implements StaticResourcesService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceSecure.class);

	protected final StaticResourcesService delegate;

	protected UserProvider provider;

	public StaticResourcesServiceSecure(StaticResourcesService delegate)
	{
		this.delegate = delegate;
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

		this.provider = provider;
	}

	@Override
	public Response getFile(String fileName, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getFile(fileName, uri, headers);
	}
}
