package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.specification.ConformanceService;

public class ConformanceServiceSecure implements ConformanceService
{
	private final ConformanceService delegate;

	public ConformanceServiceSecure(ConformanceService delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public String getPath()
	{
		throw new UnsupportedOperationException("implemented by jaxrs service layer");
	}

	public Response getMetadata(String mode, UriInfo uri, HttpHeaders headers)
	{
		return delegate.getMetadata(mode, uri, headers);
	}
}
