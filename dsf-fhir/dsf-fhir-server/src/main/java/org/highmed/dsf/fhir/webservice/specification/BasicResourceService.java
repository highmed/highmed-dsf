package org.highmed.dsf.fhir.webservice.specification;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.webservice.base.BasicService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

public interface BasicResourceService<R extends Resource> extends BasicService
{
	/**
	 * standard and conditional create
	 * 
	 * @param resource
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response create(R resource, UriInfo uri, HttpHeaders headers);

	/**
	 * read by id
	 * 
	 * @param id
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response read(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * read by id and version
	 * 
	 * @param id
	 * @param version
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response vread(String id, long version, UriInfo uri, HttpHeaders headers);

	Response history(UriInfo uri, HttpHeaders headers);

	Response history(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * standard update
	 * 
	 * @param id
	 * @param resource
	 * @param uri
	 * @param headers
	 * @return
	 */
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

	/**
	 * standard delete
	 * 
	 * @param id
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response delete(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * conditional delete
	 * 
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response delete(UriInfo uri, HttpHeaders headers);

	/**
	 * search by request parameter
	 * 
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response search(UriInfo uri, HttpHeaders headers);

	Response postValidateNew(String validatePath, Parameters parameters, UriInfo uri, HttpHeaders headers);

	Response getValidateNew(String validatePath, UriInfo uri, HttpHeaders headers);

	Response postValidateExisting(String validatePath, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers);

	Response getValidateExisting(String validatePath, String id, UriInfo uri, HttpHeaders headers);

	/**
	 *
	 * @param expungePath
	 * @param uri
	 * @param headers
	 * @return
	 */
	Response expunge(String expungePath, Parameters parameters, String id, UriInfo uri, HttpHeaders headers);
}
