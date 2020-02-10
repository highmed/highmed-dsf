package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.Bundle;

public interface RootService extends BasicService
{
	Response root(@Context UriInfo uri, @Context HttpHeaders headers);
	
	Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers);
}
