package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.NeedsAuthentication;

public interface ConformanceService extends NeedsAuthentication
{
	Response getMetadata(String mode, UriInfo uri, HttpHeaders headers);
}
