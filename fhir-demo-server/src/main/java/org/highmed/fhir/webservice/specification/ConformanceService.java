package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface ConformanceService
{
	Response getMetadata(String mode, UriInfo uri);
}
