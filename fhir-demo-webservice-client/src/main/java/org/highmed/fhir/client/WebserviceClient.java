package org.highmed.fhir.client;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface WebserviceClient
{
	String getBaseUrl();

	<R extends DomainResource> R create(R resource);

	<R extends DomainResource> R createConditionaly(R resource, String ifNoneExistCriteria);

	<R extends DomainResource> R update(R resource);

	<R extends DomainResource> R updateConditionaly(R resource, Map<String, List<String>> criteria);

	void delete(Class<? extends DomainResource> resourceClass, String id);

	void deleteConditionaly(Class<? extends DomainResource> resourceClass, Map<String, List<String>> criteria);

	<R extends DomainResource> R read(Class<R> resourceType, String id);

	<R extends DomainResource> R read(Class<R> resourceType, String id, String version);

	<R extends DomainResource> Bundle search(Class<R> resourceType, Map<String, List<String>> parameters);

	CapabilityStatement getConformance();

	StructureDefinition generateSnapshot(String url);

	StructureDefinition generateSnapshot(StructureDefinition differential);

	Bundle postBundle(Bundle bundle);
}
