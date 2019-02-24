package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.NeedsAuthentication;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Parameters;

public interface BasicService<R extends DomainResource> extends NeedsAuthentication
{
	Response create(R resource, UriInfo uri, HttpHeaders headers);

	Response read(String id, UriInfo uri, HttpHeaders headers);

	Response vread(String id, long version, UriInfo uri, HttpHeaders headers);

	Response update(String id, R resource, UriInfo uri, HttpHeaders headers);

	Response delete(String id, UriInfo uri, HttpHeaders headers);

	Response search(UriInfo uri, HttpHeaders headers);

	Response postValidateNew(String validatePath, Parameters parameters, UriInfo uri, HttpHeaders headers);

	Response getValidateNew(String validatePath, UriInfo uri, HttpHeaders headers);

	Response postValidateExisting(String validatePath, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers);

	Response getValidateExisting(String validatePath, String id, UriInfo uri, HttpHeaders headers);
}
