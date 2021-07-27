package org.highmed.dsf.fhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;

public class ResourceReference
{
	private static final Pattern TEMP_ID_PATTERN = Pattern.compile("urn:uuid:.+");

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
		 * temporary reference starting with <code>urn:uuid:</code>
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
		CONDITIONAL,
		/**
		 * temporary url in RelatedArtifact starting with <code>urn:uuid:</code>
		 */
		RELATED_ARTEFACT_TEMPORARY_URL,
		/**
		 * conditional url in RelatedArtifact
		 */
		RELATED_ARTEFACT_CONDITIONAL_URL,
		/**
		 * literal url in RelatedArtifact to a resource on this server
		 */
		RELATED_ARTEFACT_LITERAL_INTERNAL_URL,
		/**
		 * literal url in RelatedArtifact to a resource on an external server
		 */
		RELATED_ARTEFACT_LITERAL_EXTERNAL_URL, UNKNOWN
	}

	private final String location;
	private final Reference reference;
	private final RelatedArtifact relatedArtifact;
	private final List<Class<? extends Resource>> referenceTypes = new ArrayList<>();

	@SafeVarargs
	public ResourceReference(String location, Reference reference, Class<? extends Resource>... referenceTypes)
	{
		this(location, reference, null, Arrays.asList(referenceTypes));
	}

	public ResourceReference(String location, RelatedArtifact relatedArtifact)
	{
		this(location, null, relatedArtifact, Collections.emptyList());
	}

	public ResourceReference(String location, Reference reference, RelatedArtifact relatedArtifact,
			Collection<Class<? extends Resource>> referenceTypes)
	{
		this.location = location;
		this.reference = reference;
		this.relatedArtifact = relatedArtifact;

		if (referenceTypes != null)
			this.referenceTypes.addAll(referenceTypes);
	}

	public boolean hasReference()
	{
		return reference != null;
	}

	public Reference getReference()
	{
		return reference;
	}

	public boolean hasRelatedArtifact()
	{
		return relatedArtifact != null;
	}

	public RelatedArtifact getRelatedArtifact()
	{
		return relatedArtifact;
	}

	public String getValue()
	{
		if (hasReference())
			return reference.getReference();
		else if (hasRelatedArtifact())
			return relatedArtifact.getUrl();
		else
			throw new IllegalArgumentException("reference and related artefact not set");
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
	 * Determines the {@link ReferenceType} based {@link Reference#getReference()} first and then looks at
	 * {@link Reference#getIdentifier()}
	 *
	 * @param localServerBase
	 *            not <code>null</code>
	 * @return one of this priority list: {@link ReferenceType#TEMPORARY}, {@link ReferenceType#LITERAL_INTERNAL},
	 *         {@link ReferenceType#LITERAL_EXTERNAL}, {@link ReferenceType#CONDITIONAL}, {@link ReferenceType#LOGICAL},
	 *         {@link ReferenceType#UNKNOWN}
	 */
	public ReferenceType getType(String localServerBase)
	{
		Objects.requireNonNull(localServerBase, "localServerBase");

		if (relatedArtifact != null)
		{
			if (relatedArtifact.hasUrl())
			{
				Matcher tempIdRefMatcher = TEMP_ID_PATTERN.matcher(relatedArtifact.getUrl());
				if (tempIdRefMatcher.matches())
					return ReferenceType.RELATED_ARTEFACT_TEMPORARY_URL;

				Matcher idRefMatcher = ID_PATTERN.matcher(relatedArtifact.getUrl());
				if (idRefMatcher.matches())
				{
					IdType id = new IdType(relatedArtifact.getUrl());
					if (!id.isAbsolute() || localServerBase.equals(id.getBaseUrl()))
						return ReferenceType.RELATED_ARTEFACT_LITERAL_INTERNAL_URL;
					else
						return ReferenceType.RELATED_ARTEFACT_LITERAL_EXTERNAL_URL;
				}

				Matcher conditionalRefMatcher = CONDITIONAL_REF_PATTERN.matcher(relatedArtifact.getUrl());
				if (conditionalRefMatcher.matches())
					return ReferenceType.RELATED_ARTEFACT_CONDITIONAL_URL;
			}
		}
		else if (reference != null)
		{
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
		}

		return ReferenceType.UNKNOWN;
	}

	public String getLocation()
	{
		return location;
	}

	/**
	 * @param localServerBase
	 *            not <code>null</code>
	 * @return empty String if the type of this {@link ResourceReference} is not {@link ReferenceType#LITERAL_EXTERNAL}
	 */
	public String getServerBase(String localServerBase)
	{
		Objects.requireNonNull(localServerBase, "localServerBase");

		if (ReferenceType.LITERAL_EXTERNAL.equals(getType(localServerBase)))
			if (hasReference())
				return new IdType(reference.getReference()).getBaseUrl();
			else if (hasRelatedArtifact())
				return new IdType(relatedArtifact.getUrl()).getBaseUrl();

		return "";
	}
}