package org.highmed.dsf.fhir.webservice.secure;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StatusServiceSecure implements StatusService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	private final StatusService delegate;

	public StatusServiceSecure(StatusService delegate)
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

	@Override
	public Response status(UriInfo uri, HttpHeaders headers, HttpServletRequest request)
	{
		if (request.getLocalPort() != PORT)
		{
			logger.warn("Sending '401 Unauthorized' request not on status port {}", PORT);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		else
			return delegate.status(uri, headers, request);
	}
}
