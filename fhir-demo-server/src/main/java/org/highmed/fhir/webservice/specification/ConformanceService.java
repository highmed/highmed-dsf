package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.NeedsAuthentication;

public interface ConformanceService extends NeedsAuthentication
{
	Response getMetadata(String mode, UriInfo uri);
}
