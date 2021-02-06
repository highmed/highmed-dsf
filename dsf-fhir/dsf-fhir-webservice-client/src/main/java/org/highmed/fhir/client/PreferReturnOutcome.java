package org.highmed.fhir.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

public interface PreferReturnOutcome
{
	OperationOutcome create(Resource resource);

	OperationOutcome createConditionaly(Resource resource, String ifNoneExistCriteria);

	OperationOutcome createBinary(InputStream in, MediaType mediaType, String securityContextReference);


	OperationOutcome update(Resource resource);

	OperationOutcome updateConditionaly(Resource resource, Map<String, List<String>> criteria);

	OperationOutcome updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference);


	Bundle postBundle(Bundle bundle);
}