package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.NeedsAuthentication;
import org.highmed.dsf.fhir.authentication.UserProvider;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

public interface BasicService<R extends Resource> extends NeedsAuthentication
{
	void setUserProvider(UserProvider provider);

	/**
	 * regular and conditional create
	 * 
	 * @param resource
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response create(R resource, UriInfo uri, HttpHeaders headers);

	Response read(String id, UriInfo uri, HttpHeaders headers);

	Response vread(String id, long version, UriInfo uri, HttpHeaders headers);

	Response update(String id, R resource, UriInfo uri, HttpHeaders headers);

	/**
	 * conditional update
	 * 
	 * @param resource
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response update(R resource, UriInfo uri, HttpHeaders headers);

	Response delete(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * conditional delete
	 * 
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response delete(UriInfo uri, HttpHeaders headers);

	Response search(UriInfo uri, HttpHeaders headers);

	Response postValidateNew(String validatePath, Parameters parameters, UriInfo uri, HttpHeaders headers);

	Response getValidateNew(String validatePath, UriInfo uri, HttpHeaders headers);

	Response postValidateExisting(String validatePath, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers);

	Response getValidateExisting(String validatePath, String id, UriInfo uri, HttpHeaders headers);
}
