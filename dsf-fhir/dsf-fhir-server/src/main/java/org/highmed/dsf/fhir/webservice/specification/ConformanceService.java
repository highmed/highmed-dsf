package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface ConformanceService extends BasicService
{
	Response getMetadata(String mode, UriInfo uri, HttpHeaders headers);
}
