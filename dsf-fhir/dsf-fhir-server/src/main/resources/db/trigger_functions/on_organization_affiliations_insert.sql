CREATE OR REPLACE FUNCTION on_organization_affiliations_insert() RETURNS TRIGGER AS $$
DECLARE
	reference_regex TEXT := '((http|https):\/\/([A-Za-z0-9\-\\\.\:\%\$]*\/)+)?(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet|VerificationResult|VisionPrescription)\/([A-Za-z0-9\-\.]{1,64})(\/_history\/([A-Za-z0-9\-\.]{1,64}))?';
	consortium_identifier TEXT;
	member_organization_id UUID;
	insert_count INT;
	binary_insert_count INT;
	delete_count INT;
BEGIN
	PERFORM on_resources_insert(NEW.organization_affiliation_id, NEW.version, NEW.organization_affiliation);
	
	RAISE NOTICE 'NEW.organization_affiliation->>''active'' = ''%''', NEW.organization_affiliation->>'active';
	IF (NEW.organization_affiliation->>'active' = 'true') THEN
		consortium_identifier := jsonb_path_query(organization, '$.identifier[*] ? (@.system == "http://highmed.org/sid/organization-identifier")')->>'value'
			FROM current_organizations
			WHERE organization_id = (regexp_match(NEW.organization_affiliation->'organization'->>'reference', reference_regex))[5]::uuid
			AND organization->>'active' = 'true';
		member_organization_id := organization_id FROM current_organizations
			WHERE organization_id = (regexp_match(NEW.organization_affiliation->'participatingOrganization'->>'reference', reference_regex))[5]::uuid
			AND organization->>'active' = 'true';

		IF (consortium_identifier IS NOT NULL AND member_organization_id IS NOT NULL) THEN
			RAISE NOTICE 'consortium_identifier IS NOT NULL AND member_organization_id IS NOT NULL';
			INSERT INTO read_access 			
				SELECT DISTINCT r.id, r.version, 'ROLE', member_organization_id, NEW.organization_affiliation_id
				FROM (
					SELECT 
						coding->>'system' AS system
						, coding->>'code' AS code
					FROM (
						SELECT jsonb_array_elements(jsonb_array_elements(NEW.organization_affiliation->'code')->'coding') AS coding
					) AS codings
				) AS c
				LEFT JOIN (
					SELECT
						id
						, version
						, resource
					FROM all_resources
				) AS r
				ON r.resource->'meta'->'tag' @> 
					('[{"extension":[{"url":"http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role","extension":[{"url":"consortium","valueIdentifier":{"system":"http://highmed.org/sid/organization-identifier","value":"'
					|| consortium_identifier || '"}},{"url":"role","valueCoding":{"system":"'
					|| c.system || '","code":"'
					|| c.code || '"}}]}],"system":"http://highmed.org/fhir/CodeSystem/read-access-tag","code":"ROLE"}]')::jsonb
				WHERE r.resource IS NOT NULL;

			GET DIAGNOSTICS insert_count = ROW_COUNT;
			RAISE NOTICE 'Rows inserted into read_access: %', insert_count;

			INSERT INTO read_access
				SELECT binary_id, version, access_type, organization_id, organization_affiliation_id
				FROM read_access, current_binaries
				WHERE organization_id = member_organization_id
				AND organization_affiliation_id = NEW.organization_affiliation_id
				AND access_type = 'ROLE'
				AND resource_id = (regexp_match(binary_json->'securityContext'->>'reference', reference_regex))[5]::uuid;

			GET DIAGNOSTICS binary_insert_count = ROW_COUNT;
			RAISE NOTICE 'Rows inserted into read_access based on Binary.securityContext: %', binary_insert_count;
		END IF;

	ELSIF (NEW.organization_affiliation->>'active' = 'false') THEN
		DELETE FROM read_access
		WHERE access_type = 'ROLE'
		AND organization_affiliation_id = NEW.organization_affiliation_id;

		GET DIAGNOSTICS delete_count = ROW_COUNT;
		RAISE NOTICE 'Rows deleted from read_access: %', delete_count;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL