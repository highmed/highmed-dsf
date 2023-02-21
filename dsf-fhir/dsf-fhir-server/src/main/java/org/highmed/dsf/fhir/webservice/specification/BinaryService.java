package org.highmed.dsf.fhir.webservice.specification;

import java.io.InputStream;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.Binary;

public interface BinaryService extends BasicResourceService<Binary>
{
	Response create(InputStream in, UriInfo uri, HttpHeaders headers);

	Response update(String id, InputStream in, UriInfo uri, HttpHeaders headers);
}
