CREATE OR REPLACE FUNCTION on_organizations_update() RETURNS TRIGGER AS $$
DECLARE
	reference_regex TEXT := '((http|https):\/\/([A-Za-z0-9\-\\\.\:\%\$]*\/)+)?(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet|VerificationResult|VisionPrescription)\/([A-Za-z0-9\-\.]{1,64})(\/_history\/([A-Za-z0-9\-\.]{1,64}))?';
	delete_count INT;
	roles_delete_count INT;
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.organization_id, NEW.version, NEW.organization);

	IF (NEW.deleted IS NOT NULL) THEN
		DELETE FROM read_access
		WHERE access_type = 'ORGANIZATION'
		AND organization_id = NEW.organization_id;

		GET DIAGNOSTICS delete_count = ROW_COUNT;
		RAISE NOTICE 'Rows deleted from read_access: %', delete_count;
	
		DELETE FROM read_access
		WHERE access_type = 'ROLE'
		AND organization_affiliation_id IN (
			SELECT organization_affiliation_id FROM current_organization_affiliations 
			WHERE NEW.organization_id = (regexp_match(organization_affiliation->'participatingOrganization'->>'reference', reference_regex))[5]::uuid
			OR NEW.organization_id = (regexp_match(organization_affiliation->'organization'->>'reference', reference_regex))[5]::uuid
		);
		
		GET DIAGNOSTICS roles_delete_count = ROW_COUNT;
		RAISE NOTICE 'Rows deleted from read_access based on disabled roles: %', roles_delete_count;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL