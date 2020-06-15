package org.highmed.fhir.client;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface FhirWebserviceClient extends PreferReturnResource
{
	String getBaseUrl();

	PreferReturnMinimal withMinimalReturn();

	PreferReturnOutcome withOperationOutcomeReturn();

	void delete(Class<? extends Resource> resourceClass, String id);

	void deleteConditionaly(Class<? extends Resource> resourceClass, Map<String, List<String>> criteria);

	Resource read(String resourceTypeName, String id);

	<R extends Resource> R read(Class<R> resourceType, String id);

	<R extends Resource> boolean exists(Class<R> resourceType, String id);

	Resource read(String resourceTypeName, String id, String version);

	<R extends Resource> R read(Class<R> resourceType, String id, String version);

	<R extends Resource> boolean exists(Class<R> resourceType, String id, String version);

	boolean exists(IdType resourceTypeIdVersion);

	Bundle search(Class<? extends Resource> resourceType, Map<String, List<String>> parameters);

	CapabilityStatement getConformance();

	StructureDefinition generateSnapshot(String url);

	StructureDefinition generateSnapshot(StructureDefinition differential);
}
