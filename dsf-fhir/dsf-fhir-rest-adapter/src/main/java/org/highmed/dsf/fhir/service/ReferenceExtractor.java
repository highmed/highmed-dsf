package org.highmed.dsf.fhir.service;

import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

public interface ReferenceExtractor
{
	Stream<ResourceReference> getReferences(Resource resource);

	Stream<ResourceReference> getReferences(ActivityDefinition resource);

	Stream<ResourceReference> getReferences(Binary resource);

	// Not implemented yet, special rules apply for tmp ids
	// Stream<ResourceReference> getReferences(Bundle resource);

	Stream<ResourceReference> getReferences(CodeSystem resource);

	Stream<ResourceReference> getReferences(DocumentReference resource);

	Stream<ResourceReference> getReferences(Endpoint resource);

	Stream<ResourceReference> getReferences(Group resource);

	Stream<ResourceReference> getReferences(HealthcareService resource);

	Stream<ResourceReference> getReferences(Library resource);

	Stream<ResourceReference> getReferences(Location resource);

	Stream<ResourceReference> getReferences(Measure resource);

	Stream<ResourceReference> getReferences(MeasureReport resource);

	Stream<ResourceReference> getReferences(NamingSystem resource);

	Stream<ResourceReference> getReferences(OperationOutcome resource);

	Stream<ResourceReference> getReferences(Organization resource);

	Stream<ResourceReference> getReferences(OrganizationAffiliation resource);

	Stream<ResourceReference> getReferences(Patient resource);

	Stream<ResourceReference> getReferences(Practitioner resource);

	Stream<ResourceReference> getReferences(PractitionerRole resource);

	Stream<ResourceReference> getReferences(Provenance resource);

	Stream<ResourceReference> getReferences(Questionnaire resource);

	Stream<ResourceReference> getReferences(QuestionnaireResponse resource);

	Stream<ResourceReference> getReferences(ResearchStudy resource);

	Stream<ResourceReference> getReferences(StructureDefinition resource);

	Stream<ResourceReference> getReferences(Subscription resource);

	Stream<ResourceReference> getReferences(Task resource);

	Stream<ResourceReference> getReferences(ValueSet resource);
}