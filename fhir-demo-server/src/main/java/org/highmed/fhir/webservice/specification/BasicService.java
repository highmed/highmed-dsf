package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.NeedsAuthentication;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Parameters;

public interface BasicService<R extends DomainResource> extends NeedsAuthentication
{
	Response create(R resource, UriInfo uri);

	Response read(String id, String format, UriInfo uri);

	Response vread(String id, long version, String format, UriInfo uri);

	Response update(String id, R resource, UriInfo uri);

	Response delete(String id, UriInfo uri);

	Response search(UriInfo uri);

	Response postValidateNew(String validate, Parameters parameters, UriInfo uri);

	Response getValidateNew(String validate, String mode, String profile, String format, UriInfo uri);

	Response postValidateExisting(String validate, Parameters parameters, UriInfo uri);

	Response getValidateExisting(String validate, String mode, String profile, String format, UriInfo uri);
}
