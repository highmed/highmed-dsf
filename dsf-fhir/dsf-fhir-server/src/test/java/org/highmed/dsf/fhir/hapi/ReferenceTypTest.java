package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ReferenceTypTest
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceTypTest.class);

	private static final Pattern ID_PATTERN = Pattern
			.compile("(?<base>(http|https):\\/\\/([A-Za-z0-9\\-\\\\\\.\\:\\%\\$]*\\/)+)?"
					+ "(?<resource>Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse"
					+ "|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement"
					+ "|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse"
					+ "|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition"
					+ "|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest"
					+ "|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest"
					+ "|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis"
					+ "|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence"
					+ "|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal"
					+ "|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization"
					+ "|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice"
					+ "|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration"
					+ "|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct"
					+ "|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication"
					+ "|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured"
					+ "|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect"
					+ "|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation"
					+ "|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation"
					+ "|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole"
					+ "|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup"
					+ "|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment"
					+ "|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition"
					+ "|StructureDefinition|StructureMap|Subscription|Substance|SubstanceDefinition|SubstanceNucleicAcid"
					+ "|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial"
					+ "|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet"
					+ "|VerificationResult|VisionPrescription)"
					+ "\\/(?<id>[A-Za-z0-9\\-\\.]{1,64})(?:\\/_history\\/(?<version>[A-Za-z0-9\\-\\.]{1,64}))?");

	private static final Pattern CONDITIONAL_REF_PATTERN = Pattern.compile(
			"(?<resource>Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse"
					+ "|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement"
					+ "|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse"
					+ "|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition"
					+ "|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest"
					+ "|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest"
					+ "|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis"
					+ "|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence"
					+ "|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal"
					+ "|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization"
					+ "|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice"
					+ "|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration"
					+ "|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct"
					+ "|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication"
					+ "|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured"
					+ "|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect"
					+ "|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation"
					+ "|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation"
					+ "|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole"
					+ "|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup"
					+ "|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment"
					+ "|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition"
					+ "|StructureDefinition|StructureMap|Subscription|Substance|SubstanceDefinition|SubstanceNucleicAcid"
					+ "|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial"
					+ "|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet"
					+ "|VerificationResult|VisionPrescription)" + "(?<query>\\?.*)");

	@Test
	public void testRegex() throws Exception
	{
		String uuid = UUID.randomUUID().toString();

		var m1 = ID_PATTERN.matcher("Patient/" + uuid);
		assertTrue(m1.matches());
		assertEquals(null, m1.group("base"));
		assertEquals("Patient", m1.group("resource"));
		assertEquals(uuid, m1.group("id"));
		assertEquals(null, m1.group("version"));

		var m2 = ID_PATTERN.matcher("https://foo.bar/fhir/Patient/" + uuid);
		assertTrue(m2.matches());
		assertEquals("https://foo.bar/fhir/", m2.group("base"));
		assertEquals("Patient", m2.group("resource"));
		assertEquals(uuid, m2.group("id"));
		assertEquals(null, m2.group("version"));

		var m3 = ID_PATTERN.matcher("Patient/" + uuid + "/_history/234");
		assertTrue(m3.matches());
		assertEquals(null, m3.group("base"));
		assertEquals("Patient", m3.group("resource"));
		assertEquals(uuid, m3.group("id"));
		assertEquals("234", m3.group("version"));

		var m4 = ID_PATTERN.matcher("https://foo.bar/fhir/Patient/" + uuid + "/_history/234");
		assertTrue(m4.matches());
		assertEquals("https://foo.bar/fhir/", m4.group("base"));
		assertEquals("Patient", m4.group("resource"));
		assertEquals(uuid, m4.group("id"));
		assertEquals("234", m4.group("version"));

		var m5 = ID_PATTERN.matcher("Patient?foo=bar");
		assertFalse(m5.matches());

		var m6 = ID_PATTERN.matcher("https://foo.bar/fhir/Patient/" + uuid + "?foo=bar");
		assertFalse(m6.matches());

		var m7 = CONDITIONAL_REF_PATTERN.matcher("Patient?foo=bar");
		assertTrue(m7.matches());
		assertEquals("Patient", m7.group("resource"));
		assertEquals("?foo=bar", m7.group("query"));

		var m8 = CONDITIONAL_REF_PATTERN.matcher("Patient?foo=bar&baz=baz");
		assertTrue(m8.matches());
		assertEquals("Patient", m8.group("resource"));
		assertEquals("?foo=bar&baz=baz", m8.group("query"));
	}

	@Test
	public void testIdType() throws Exception
	{
		String uuid = UUID.randomUUID().toString();

		var id1 = new IdType("https://foo.bar/fhir/Patient/" + uuid + "/_history/234");
		assertEquals("https://foo.bar/fhir", id1.getBaseUrl());
		assertEquals("Patient", id1.getResourceType());
		assertEquals(uuid, id1.getIdPart());
		assertEquals("234", id1.getVersionIdPart());
		assertFalse(id1.isLocal());
		assertFalse(id1.isUrn());
		assertTrue(id1.isAbsolute());

		var id2 = new IdType("Patient/" + uuid + "/_history/234");
		assertEquals(null, id2.getBaseUrl());
		assertEquals("Patient", id2.getResourceType());
		assertEquals(uuid, id2.getIdPart());
		assertEquals("234", id2.getVersionIdPart());
		assertFalse(id2.isLocal());
		assertFalse(id2.isUrn());
		assertFalse(id2.isAbsolute());

		var id3 = new IdType("urn:uuid:" + uuid);
		assertEquals(null, id3.getBaseUrl());
		assertEquals(null, id3.getResourceType());
		assertEquals("urn:uuid:" + uuid, id3.getIdPart());
		assertEquals(null, id3.getVersionIdPart());
		assertFalse(id3.isLocal());
		assertTrue(id3.isUrn());
		assertFalse(id3.isAbsolute());

		var id4 = new IdType("Patient?foo=bar");
		assertEquals(null, id4.getBaseUrl());
		assertEquals(null, id4.getResourceType());
		assertEquals("Patient?foo=bar", id4.getIdPart());
		assertEquals(null, id4.getVersionIdPart());
		assertFalse(id4.isLocal());
		assertFalse(id4.isUrn());
		assertFalse(id4.isAbsolute());

		var id5 = new IdType("#" + uuid);
		assertEquals(null, id5.getBaseUrl());
		assertEquals(null, id5.getResourceType());
		assertEquals("#" + uuid, id5.getIdPart());
		assertEquals(null, id5.getVersionIdPart());
		assertTrue(id5.isLocal());
		assertFalse(id5.isUrn());
		assertFalse(id5.isAbsolute());
	}

	@Test
	public void testType() throws Exception
	{
		// Reference ref = new Reference();
		// ref.setType("Organization");
		// ref.setReferenceElement(new IdType().setParts(null, "Organization", UUID.randomUUID().toString(), null));

		Organization org = new Organization();
		org.setIdElement(new IdType(UUID.randomUUID().toString()));

		Binary b = new Binary();
		Reference ref = new Reference(org);
		b.setSecurityContext(ref);
		b.getSecurityContext().setType("Organization");

		FhirContext context = FhirContext.forR4();
		String string = context.newXmlParser().encodeResourceToString(b);

		logger.debug(string);
	}
}
