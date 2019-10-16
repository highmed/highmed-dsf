package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootServiceSecure implements RootService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceSecure.class);

	protected final RootService delegate;

	protected UserProvider provider;

	public RootServiceSecure(RootService delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void setUserProvider(UserProvider provider)
	{
		delegate.setUserProvider(provider);

		this.provider = provider;
	}

	@Override
	public String getPath()
	{
		throw new UnsupportedOperationException("implemented by jaxrs service layer");
	}

	@Override
	public Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.handleBundle(bundle, uri, headers);
	}
}
