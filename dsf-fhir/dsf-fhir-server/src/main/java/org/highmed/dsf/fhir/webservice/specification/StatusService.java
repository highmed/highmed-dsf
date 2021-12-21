package org.highmed.dsf.fhir.webservice.specification;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.DoesNotNeedAuthentication;

public interface StatusService extends DoesNotNeedAuthentication
{
	int PORT = 10001;

	Response status(UriInfo uri, HttpHeaders headers, HttpServletRequest httpServletRequest);
}
