package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.ConformanceService;

public class ConformanceServiceSecure implements ConformanceService
{
	private final ConformanceService delegate;

	public ConformanceServiceSecure(ConformanceService delegate)
	{
		this.delegate = delegate;
	}

	public Response getMetadata(String mode, UriInfo uri, HttpHeaders headers)
	{
		return delegate.getMetadata(mode, uri, headers);
	}
}
