package org.highmed.fhir.client;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface WebserviceClient
{
	<R extends DomainResource> R create(R resource);

	<R extends DomainResource> R update(R resource);

	CapabilityStatement getConformance();

	StructureDefinition generateSnapshot(String url);

	StructureDefinition generateSnapshot(StructureDefinition differential);
}
