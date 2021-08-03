CREATE OR REPLACE FUNCTION on_resources_insert(new_resource_id uuid, new_resource_version bigint, new_resource jsonb) RETURNS void AS $$
DECLARE
	reference_regex TEXT := '((http|https):\/\/([A-Za-z0-9\-\\\.\:\%\$]*\/)+)?(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet|VerificationResult|VisionPrescription)\/([A-Za-z0-9\-\.]{1,64})(\/_history\/([A-Za-z0-9\-\.]{1,64}))?';
	binary_delete_count INT;
	all_insert_count INT := 0;
	local_insert_count INT := 0;
	organization_insert_count INT := 0;
	role_insert_count INT := 0;
	binary_insert_count INT;
BEGIN
	-- delete entries of binaries that use this new/updated resource as security context (id or id/version reference)
	DELETE FROM read_access WHERE EXISTS (
		SELECT 1 FROM current_binaries 
		WHERE read_access.resource_id = binary_id
		AND read_access.resource_version = version
		AND new_resource_id = (regexp_match(binary_json->'securityContext'->>'reference', reference_regex))[5]::uuid
		AND new_resource_version = COALESCE((regexp_match(binary_json->'securityContext'->>'reference', reference_regex))[7]::bigint, new_resource_version)
	);
	
	GET DIAGNOSTICS binary_delete_count = ROW_COUNT;
	RAISE NOTICE 'Rows deleted from read_access based on Binary.securityContext: %', binary_delete_count;

	-- add entries for ALL if tag exists
	IF (new_resource->'meta'->'tag' @> '[{"system":"http://highmed.org/fhir/CodeSystem/read-access-tag","code":"ALL"}]'::jsonb) THEN
		INSERT INTO read_access
		VALUES(new_resource_id, new_resource_version, 'ALL', NULL, NULL);
		
		GET DIAGNOSTICS all_insert_count = ROW_COUNT;
	END IF;
	
	-- add entries for LOCAL if tag exists
	IF (new_resource->'meta'->'tag' @> '[{"system":"http://highmed.org/fhir/CodeSystem/read-access-tag","code":"LOCAL"}]'::jsonb) THEN
		INSERT INTO read_access
		VALUES(new_resource_id, new_resource_version, 'LOCAL', NULL, NULL);

		GET DIAGNOSTICS local_insert_count = ROW_COUNT;
	END IF;
	
	-- add entries for ORGANIZATION if tag exists
	IF (new_resource->'meta'->'tag' @> '[{"system":"http://highmed.org/fhir/CodeSystem/read-access-tag","code":"ORGANIZATION"}]'::jsonb) THEN
		INSERT INTO read_access 			
		SELECT new_resource_id, new_resource_version,'ORGANIZATION', organization_id, NULL FROM (
			SELECT organization_id, jsonb_path_query(organization, '$.identifier[*] ? (@.system == "http://highmed.org/sid/organization-identifier")')->>'value' AS organization_identifier
			FROM current_organizations WHERE organization->>'active' = 'true'
		) AS organizations 
		WHERE organization_identifier IN (
			SELECT jsonb_path_query(new_resource,'$.meta.tag[*] ? (@.code == "ORGANIZATION" && @.system == "http://highmed.org/fhir/CodeSystem/read-access-tag")
				.extension[*]?(@.url == "http://highmed.org/fhir/StructureDefinition/extension-read-access-organization")
				.valueIdentifier[*]?(@.system == "http://highmed.org/sid/organization-identifier")')->>'value'
		);

		GET DIAGNOSTICS organization_insert_count = ROW_COUNT;
	END IF;
	
	-- add entries for ROLE if tag exists
	IF (new_resource->'meta'->'tag' @> '[{"system":"http://highmed.org/fhir/CodeSystem/read-access-tag","code":"ROLE"}]'::jsonb) THEN
		INSERT INTO read_access 			
		SELECT new_resource_id, new_resource_version, 'ROLE', member_organization_id, organization_affiliation_id FROM (
			SELECT DISTINCT member_organization_id, organization_affiliation_id FROM (
				SELECT
					organization_affiliation_id
					, (regexp_match(organization_affiliation->'participatingOrganization'->>'reference', reference_regex))[5]::uuid AS member_organization_id
					, (
						SELECT jsonb_path_query(organization, '$.identifier[*]?(@.system == "http://highmed.org/sid/organization-identifier")')->>'value' AS parent_organization_identifier
						FROM current_organizations WHERE organization->>'active' = 'true' AND organization->>'id' = (regexp_match(organization_affiliation->'organization'->>'reference', reference_regex))[5] 
					)
					, codes AS role
				FROM current_organization_affiliations, jsonb_array_elements(organization_affiliation->'code') AS codings, jsonb_array_elements(codings->'coding') AS codes
				WHERE organization_affiliation->>'active' = 'true'
			) AS oa
			LEFT JOIN (
				SELECT 
					jsonb_path_query(consortium_role, '$.extension[*] ? (@.url == "consortium")
						.valueIdentifier[*]?(@.system == "http://highmed.org/sid/organization-identifier")')->>'value' AS consortium_identifier
					, jsonb_path_query(consortium_role, '$.extension[*] ? (@.url == "role").valueCoding') AS role
				FROM (
					SELECT jsonb_path_query(new_resource,'$.meta.tag[*] ? (@.code == "ROLE" && @.system == "http://highmed.org/fhir/CodeSystem/read-access-tag")
						.extension[*] ? (@.url == "http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role")') AS consortium_role
				) AS cr
			) AS t
			ON oa.parent_organization_identifier = t.consortium_identifier AND oa.role->>'system' = t.role->>'system' AND oa.role->>'code' = t.role->>'code'
			WHERE t.consortium_identifier IS NOT NULL AND t.role IS NOT NULL
		) AS member_organizations;

		GET DIAGNOSTICS role_insert_count = ROW_COUNT;
	END IF;
	
	RAISE NOTICE 'Rows inserted into read_access: %', (all_insert_count + local_insert_count + organization_insert_count + role_insert_count);
	
	-- add entries for binaries that use this new/updated resource as security context (id or id/version reference)
	INSERT INTO read_access
		SELECT binary_id, version, access_type, organization_id, organization_affiliation_id
		FROM read_access, current_binaries
		WHERE read_access.resource_id = new_resource_id
		AND read_access.resource_id = (regexp_match(binary_json->'securityContext'->>'reference', reference_regex))[5]::uuid
		AND read_access.resource_version = new_resource_version
		AND read_access.resource_version = COALESCE((regexp_match(binary_json->'securityContext'->>'reference', reference_regex))[7]::bigint, read_access.resource_version);
	
	GET DIAGNOSTICS binary_insert_count = ROW_COUNT;
	RAISE NOTICE 'Rows inserted into read_access based on Binary.securityContext: %', binary_insert_count;
END;
$$ LANGUAGE PLPGSQL