package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public interface PreferReturnResource
{
	<R extends Resource> R create(R resource);

	<R extends Resource> R createConditionaly(R resource, String ifNoneExistCriteria);

	Binary createBinary(InputStream in, MediaType mediaType, String securityContextReference);


	<R extends Resource> R update(R resource);

	<R extends Resource> R updateConditionaly(R resource, Map<String, List<String>> criteria);

	Binary updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference);


	Bundle postBundle(Bundle bundle);
}