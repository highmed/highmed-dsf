package org.highmed.dsf.fhir.webservice.specification;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.base.BasicService;

public interface StaticResourcesService extends BasicService
{
	Response getFile(String fileName, UriInfo uri, HttpHeaders headers);
}
