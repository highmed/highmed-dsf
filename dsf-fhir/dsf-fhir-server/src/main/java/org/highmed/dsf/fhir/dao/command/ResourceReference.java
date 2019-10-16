package org.highmed.dsf.fhir.dao.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class ResourceReference
{
	private static final Pattern TEMP_ID_PATTERN = Pattern.compile(Command.URL_UUID_PREFIX + ".+");

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

	public static enum ReferenceType
	{
		/**
		 * temporary reference starting with {@link Command#URL_UUID_PREFIX}
		 */
		TEMPORARY,
		/**
		 * literal reference to a resource on this server
		 */
		LITERAL_INTERNAL,
		/**
		 * literal reference to a resource on a different server
		 */
		LITERAL_EXTERNAL,
		/**
		 * logical reference with type and identifier system and identifier value
		 */
		LOGICAL,
		/**
		 * conditional reference as used in batch transactions
		 */
		CONDITIONAL, UNKNOWN
	}

	private final String referenceLocation;
	private final Reference reference;
	private final List<Class<? extends Resource>> referenceTypes = new ArrayList<>();

	public ResourceReference(String referenceLocation, Reference reference,
			List<Class<? extends Resource>> referenceTypes)
	{
		this.referenceLocation = referenceLocation;
		this.reference = reference;

		if (referenceTypes != null)
			this.referenceTypes.addAll(referenceTypes);
	}

	public Reference getReference()
	{
		return reference;
	}

	public List<Class<? extends Resource>> getReferenceTypes()
	{
		return Collections.unmodifiableList(referenceTypes);
	}

	public boolean supportsType(Class<? extends Resource> type)
	{
		return referenceTypes.isEmpty() || referenceTypes.contains(type);
	}

	/**
	 * @param localServerBase
	 *            not <code>null</code>
	 * @return
	 */
	public ReferenceType getType(String localServerBase)
	{
		Objects.requireNonNull(localServerBase, "localServerBase");

		if (reference.hasReference())
		{
			Matcher tempIdRefMatcher = TEMP_ID_PATTERN.matcher(reference.getReference());
			if (tempIdRefMatcher.matches())
				return ReferenceType.TEMPORARY;

			Matcher idRefMatcher = ID_PATTERN.matcher(reference.getReference());
			if (idRefMatcher.matches())
			{
				IdType id = new IdType(reference.getReference());
				if (!id.isAbsolute() || localServerBase.equals(id.getBaseUrl()))
					return ReferenceType.LITERAL_INTERNAL;
				else
					return ReferenceType.LITERAL_EXTERNAL;
			}

			Matcher conditionalRefMatcher = CONDITIONAL_REF_PATTERN.matcher(reference.getReference());
			if (conditionalRefMatcher.matches())
				return ReferenceType.CONDITIONAL;
		}
		else if (reference.hasType() && reference.hasIdentifier() && reference.getIdentifier().hasSystem()
				&& reference.getIdentifier().hasValue())
		{
			return ReferenceType.LOGICAL;
		}

		return ReferenceType.UNKNOWN;
	}

	public String getReferenceLocation()
	{
		return referenceLocation;
	}

	/**
	 * @return empty String if the type of this {@link ResourceReference} is not {@link ReferenceType#LITERAL_EXTERNAL}
	 * @param localServerBase
	 *            not <code>null</code>
	 */
	public String getServerBase(String localServerBase)
	{
		Objects.requireNonNull(localServerBase, "localServerBase");

		if (ReferenceType.LITERAL_EXTERNAL.equals(getType(localServerBase)))
			return new IdType(reference.getReference()).getBaseUrl();
		else
			return "";
	}
}