package org.highmed.dsf.fhir.service;

import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.ATTACHMENT_CONDITIONAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.ATTACHMENT_LITERAL_EXTERNAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.ATTACHMENT_LITERAL_INTERNAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.ATTACHMENT_TEMPORARY_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.ATTACHMENT_UNKNOWN_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.CONDITIONAL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.LITERAL_EXTERNAL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.LITERAL_INTERNAL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.LOGICAL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.RELATED_ARTEFACT_CONDITIONAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.RELATED_ARTEFACT_LITERAL_EXTERNAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.RELATED_ARTEFACT_LITERAL_INTERNAL_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.RELATED_ARTEFACT_TEMPORARY_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.RELATED_ARTEFACT_UNKNOWN_URL;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.TEMPORARY;
import static org.highmed.dsf.fhir.service.ResourceReference.ReferenceType.UNKNOWN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Attachment;
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
		 * unknown reference
		 */
		UNKNOWN,
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
		RELATED_ARTEFACT_LITERAL_EXTERNAL_URL,
		/**
		 * unknown url in RelatedArtifact
		 */
		RELATED_ARTEFACT_UNKNOWN_URL,
		/**
		 * temporary url in Attachment starting with <code>urn:uuid:</code>
		 */
		ATTACHMENT_TEMPORARY_URL,
		/**
		 * conditional url in Attachment
		 */
		ATTACHMENT_CONDITIONAL_URL,
		/**
		 * literal url in Attachment to a resource on this server
		 */
		ATTACHMENT_LITERAL_INTERNAL_URL,
		/**
		 * literal url in Attachment to a resource on an external server
		 */
		ATTACHMENT_LITERAL_EXTERNAL_URL,
		/**
		 * unknown url in Attachment
		 */
		ATTACHMENT_UNKNOWN_URL
	}

	private final String location;
	private final Reference reference;
	private final RelatedArtifact relatedArtifact;
	private final Attachment attachment;
	private final List<Class<? extends Resource>> referenceTypes = new ArrayList<>();

	@SafeVarargs
	public ResourceReference(String location, Reference reference, Class<? extends Resource>... referenceTypes)
	{
		this(location, reference, null, null, Arrays.asList(referenceTypes));
	}

	public ResourceReference(String location, RelatedArtifact relatedArtifact)
	{
		this(location, null, relatedArtifact, null, Collections.emptyList());
	}

	public ResourceReference(String location, Attachment attachment)
	{
		this(location, null, null, attachment, Collections.emptyList());
	}

	private ResourceReference(String location, Reference reference, RelatedArtifact relatedArtifact,
			Attachment attachment, Collection<Class<? extends Resource>> referenceTypes)
	{
		this.location = location;

		if (reference == null && relatedArtifact == null && attachment == null)
			throw new IllegalArgumentException("Either reference, relatedArtifact or attachment expected");

		this.reference = reference;
		this.relatedArtifact = relatedArtifact;
		this.attachment = attachment;

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

	public boolean hasAttachment()
	{
		return attachment != null;
	}

	public Attachment getAttachment()
	{
		return attachment;
	}

	public String getValue()
	{
		if (hasReference())
			return reference.getReference();
		else if (hasRelatedArtifact())
			return relatedArtifact.getUrl();
		else if (hasAttachment())
			return attachment.getUrl();
		else
			throw new IllegalArgumentException("reference, related artefact or attachment not set");
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
	 * Determines the {@link ReferenceType} based on the actual FHIR reference, related artifact or attachment
	 *
	 * @param localServerBase
	 *            not <code>null</code>
	 * @return one of this priority list: {@link ReferenceType#RELATED_ARTEFACT_TEMPORARY_URL},
	 *         {@link ReferenceType#RELATED_ARTEFACT_LITERAL_INTERNAL_URL},
	 *         {@link ReferenceType#RELATED_ARTEFACT_LITERAL_EXTERNAL_URL},
	 *         {@link ReferenceType#RELATED_ARTEFACT_CONDITIONAL_URL},
	 *         {@link ReferenceType#RELATED_ARTEFACT_UNKNOWN_URL}, {@link ReferenceType#ATTACHMENT_TEMPORARY_URL},
	 *         {@link ReferenceType#ATTACHMENT_LITERAL_INTERNAL_URL},
	 *         {@link ReferenceType#ATTACHMENT_LITERAL_EXTERNAL_URL}, {@link ReferenceType#ATTACHMENT_CONDITIONAL_URL},
	 *         {@link ReferenceType#ATTACHMENT_UNKNOWN_URL}, {@link ReferenceType#TEMPORARY},
	 *         {@link ReferenceType#LITERAL_INTERNAL}, {@link ReferenceType#LITERAL_EXTERNAL},
	 *         {@link ReferenceType#CONDITIONAL}, {@link ReferenceType#LOGICAL}, {@link ReferenceType#UNKNOWN}
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
					return RELATED_ARTEFACT_TEMPORARY_URL;

				Matcher idRefMatcher = ID_PATTERN.matcher(relatedArtifact.getUrl());
				if (idRefMatcher.matches())
				{
					IdType id = new IdType(relatedArtifact.getUrl());
					if (!id.isAbsolute() || localServerBase.equals(id.getBaseUrl()))
						return RELATED_ARTEFACT_LITERAL_INTERNAL_URL;
					else
						return RELATED_ARTEFACT_LITERAL_EXTERNAL_URL;
				}

				Matcher conditionalRefMatcher = CONDITIONAL_REF_PATTERN.matcher(relatedArtifact.getUrl());
				if (conditionalRefMatcher.matches())
					return RELATED_ARTEFACT_CONDITIONAL_URL;
			}

			return RELATED_ARTEFACT_UNKNOWN_URL;
		}
		else if (attachment != null)
		{
			if (attachment.hasUrl())
			{
				Matcher tempIdRefMatcher = TEMP_ID_PATTERN.matcher(attachment.getUrl());
				if (tempIdRefMatcher.matches())
					return ATTACHMENT_TEMPORARY_URL;

				Matcher idRefMatcher = ID_PATTERN.matcher(attachment.getUrl());
				if (idRefMatcher.matches())
				{
					IdType id = new IdType(attachment.getUrl());
					if (!id.isAbsolute() || localServerBase.equals(id.getBaseUrl()))
						return ATTACHMENT_LITERAL_INTERNAL_URL;
					else
						return ATTACHMENT_LITERAL_EXTERNAL_URL;
				}

				Matcher conditionalRefMatcher = CONDITIONAL_REF_PATTERN.matcher(attachment.getUrl());
				if (conditionalRefMatcher.matches())
					return ATTACHMENT_CONDITIONAL_URL;
			}

			return ATTACHMENT_UNKNOWN_URL;
		}
		else if (reference != null)
		{
			if (reference.hasReference())
			{
				Matcher tempIdRefMatcher = TEMP_ID_PATTERN.matcher(reference.getReference());
				if (tempIdRefMatcher.matches())
					return TEMPORARY;

				Matcher idRefMatcher = ID_PATTERN.matcher(reference.getReference());
				if (idRefMatcher.matches())
				{
					IdType id = new IdType(reference.getReference());
					if (!id.isAbsolute() || localServerBase.equals(id.getBaseUrl()))
						return LITERAL_INTERNAL;
					else
						return LITERAL_EXTERNAL;
				}

				Matcher conditionalRefMatcher = CONDITIONAL_REF_PATTERN.matcher(reference.getReference());
				if (conditionalRefMatcher.matches())
					return CONDITIONAL;
			}
			else if (reference.hasType() && reference.hasIdentifier() && reference.getIdentifier().hasSystem()
					&& reference.getIdentifier().hasValue())
			{
				return LOGICAL;
			}

			return UNKNOWN;
		}
		else
			throw new IllegalStateException("Either reference or relatedArtifact expected");
	}

	public String getLocation()
	{
		return location;
	}

	/**
	 * @param localServerBase
	 *            not <code>null</code>
	 * @return empty String if the type of this {@link ResourceReference} is not {@link ReferenceType#LITERAL_EXTERNAL},
	 *         {@link ReferenceType#RELATED_ARTEFACT_LITERAL_EXTERNAL_URL} or
	 *         {@link ReferenceType#ATTACHMENT_LITERAL_EXTERNAL_URL}
	 */
	public String getServerBase(String localServerBase)
	{
		Objects.requireNonNull(localServerBase, "localServerBase");

		if (EnumSet.of(LITERAL_EXTERNAL, RELATED_ARTEFACT_LITERAL_EXTERNAL_URL, ATTACHMENT_LITERAL_EXTERNAL_URL)
				.contains(getType(localServerBase)))
			return new IdType(getValue()).getBaseUrl();
		else
			return "";
	}
}