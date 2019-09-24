package org.highmed.fhir.client;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface FhirWebserviceClient
{
	String getBaseUrl();

	<R extends Resource> R create(R resource);

	<R extends Resource> R createConditionaly(R resource, String ifNoneExistCriteria);

	<R extends Resource> R update(R resource);

	<R extends Resource> R updateConditionaly(R resource, Map<String, List<String>> criteria);

	void delete(Class<? extends Resource> resourceClass, String id);

	void deleteConditionaly(Class<? extends Resource> resourceClass, Map<String, List<String>> criteria);

	<R extends Resource> R read(Class<R> resourceType, String id);

	<R extends Resource> boolean exists(Class<R> resourceType, String id);

	<R extends Resource> R read(Class<R> resourceType, String id, String version);

	<R extends Resource> boolean exists(Class<R> resourceType, String id, String version);

	boolean exists(IdType resourceTypeIdVersion);

	<R extends Resource> Bundle search(Class<R> resourceType, Map<String, List<String>> parameters);

	CapabilityStatement getConformance();

	StructureDefinition generateSnapshot(String url);

	StructureDefinition generateSnapshot(StructureDefinition differential);

	Bundle postBundle(Bundle bundle);

}
