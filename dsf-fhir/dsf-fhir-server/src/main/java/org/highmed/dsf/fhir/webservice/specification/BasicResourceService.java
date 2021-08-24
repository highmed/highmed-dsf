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
	 *            not <code>null</code>
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#create">https://www.hl7.org/fhir/http.html#create</a>
	 */
	Response create(R resource, UriInfo uri, HttpHeaders headers);

	/**
	 * read by id
	 *
	 * @param id
	 *            not <code>null</code>
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#read">https://www.hl7.org/fhir/http.html#read</a>
	 */
	Response read(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * read by id and version
	 *
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            {@code >0}
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#vread">https://www.hl7.org/fhir/http.html#vread</a>
	 */
	Response vread(String id, long version, UriInfo uri, HttpHeaders headers);

	Response history(UriInfo uri, HttpHeaders headers);

	Response history(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * standard update
	 *
	 * @param id
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#update">https://www.hl7.org/fhir/http.html#update</a>
	 */
	Response update(String id, R resource, UriInfo uri, HttpHeaders headers);

	/**
	 * conditional update
	 *
	 * @param resource
	 *            not <code>null</code>
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#update">https://www.hl7.org/fhir/http.html#update</a>
	 */
	Response update(R resource, UriInfo uri, HttpHeaders headers);

	/**
	 * standard delete
	 *
	 * @param id
	 *            not <code>null</code>
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#delete">https://www.hl7.org/fhir/http.html#delete</a>
	 */
	Response delete(String id, UriInfo uri, HttpHeaders headers);

	/**
	 * conditional delete
	 *
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#delete">https://www.hl7.org/fhir/http.html#delete</a>
	 */
	Response delete(UriInfo uri, HttpHeaders headers);

	/**
	 * search by request parameter
	 *
	 * @param uri
	 *            not <code>null</code>
	 * @param headers
	 *            not <code>null</code>
	 * @return {@link Response} defined in
	 *         <a href="https://www.hl7.org/fhir/http.html#search">https://www.hl7.org/fhir/http.html#search</a>
	 */
	Response search(UriInfo uri, HttpHeaders headers);

	Response postValidateNew(String validatePath, Parameters parameters, UriInfo uri, HttpHeaders headers);

	Response getValidateNew(String validatePath, UriInfo uri, HttpHeaders headers);

	Response postValidateExisting(String validatePath, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers);

	Response getValidateExisting(String validatePath, String id, UriInfo uri, HttpHeaders headers);

	Response deletePermanently(String deletePath, String id, UriInfo uri, HttpHeaders headers);
}
