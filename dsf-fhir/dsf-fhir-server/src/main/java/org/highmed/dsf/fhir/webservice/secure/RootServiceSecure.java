package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootServiceSecure extends AbstractServiceSecure<RootService> implements RootService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	public RootServiceSecure(RootService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response root(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.root(uri, headers);
	}

	@Override
	public Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.handleBundle(bundle, uri, headers);
	}
}
