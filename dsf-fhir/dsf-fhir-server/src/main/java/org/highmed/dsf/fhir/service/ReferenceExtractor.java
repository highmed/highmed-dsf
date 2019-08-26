package org.highmed.dsf.fhir.service;

import java.util.stream.Stream;

import org.highmed.dsf.fhir.dao.command.ResourceReference;
import org.hl7.fhir.r4.model.*;

public interface ReferenceExtractor
{
	Stream<ResourceReference> getReferences(Resource resource);

	Stream<ResourceReference> getReferences(Endpoint resource);

	Stream<ResourceReference> getReferences(Group group);

	Stream<ResourceReference> getReferences(HealthcareService resource);

	Stream<ResourceReference> getReferences(Location resource);

	Stream<ResourceReference> getReferences(Organization resource);

	Stream<ResourceReference> getReferences(Patient resource);

	Stream<ResourceReference> getReferences(Practitioner resource);

	Stream<ResourceReference> getReferences(PractitionerRole resource);

	Stream<ResourceReference> getReferences(Provenance resource);

	Stream<ResourceReference> getReferences(ResearchStudy resource);

	Stream<ResourceReference> getReferences(Task resource);
}