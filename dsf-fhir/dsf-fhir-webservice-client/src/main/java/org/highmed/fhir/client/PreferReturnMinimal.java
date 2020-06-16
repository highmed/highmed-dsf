package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;

public interface PreferReturnMinimal
{
	IdType create(Resource resource);

	IdType createConditionaly(Resource resource, String ifNoneExistCriteria);

	IdType createBinary(InputStream in, MediaType mediaType, String securityContextReference);

	IdType update(Resource resource);

	IdType updateConditionaly(Resource resource, Map<String, List<String>> criteria);

	IdType updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference);

	Bundle postBundle(Bundle bundle);
}