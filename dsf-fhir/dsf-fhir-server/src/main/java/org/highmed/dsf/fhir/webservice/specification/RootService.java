package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.NeedsAuthentication;
import org.highmed.dsf.fhir.authentication.UserProvider;
import org.hl7.fhir.r4.model.Bundle;

public interface RootService extends NeedsAuthentication
{
	void setUserProvider(UserProvider provider);

	Response root(@Context UriInfo uri, @Context HttpHeaders headers);
	
	Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers);
}
