package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.NeedsAuthentication;
import org.highmed.fhir.authentication.UserProvider;
import org.hl7.fhir.r4.model.Bundle;

public interface RootService extends NeedsAuthentication
{
	void setUserProvider(UserProvider provider);

	Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers);
}
