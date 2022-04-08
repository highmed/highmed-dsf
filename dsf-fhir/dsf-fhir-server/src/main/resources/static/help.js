function showHelp() {
	httpRequest = new XMLHttpRequest();
	if (httpRequest != null) {
		httpRequest.onreadystatechange = createAndShowHelp;
		httpRequest.open('GET', '/fhir/metadata');
		httpRequest.setRequestHeader('Accept', 'application/fhir+json');
		httpRequest.send();
	} else {
		createAndShowHelp();
	}
}

function closeHelp() {
	var help = document.getElementById('help');
	help.style.display = 'none';
}

function createAndShowHelp() {
	if (httpRequest != null && httpRequest.readyState === XMLHttpRequest.DONE) {
		if (httpRequest.status === 200) {
			var metadata = JSON.parse(httpRequest.responseText);
			var resourceType = getResourceTypeForCurrentUrl();

			/* /, /metadata, /_history */
			if (resourceType == null) {
				var searchParam = metadata.rest[0].resource[0].searchParam;
				if (window.location.pathname.endsWith('/metadata')) {
					createHelp(searchParam.filter(p => ['_format', '_pretty'].includes(p.name)));
				} else if (window.location.pathname.endsWith('/_history')) {
					createHelp(searchParam.filter(p => ['_count', '_format', '_page', '_pretty'].includes(p.name)));
				} else {
					createHelp(searchParam.filter(p => ['_format', '_pretty'].includes(p.name)));
				}
			}
			else {
				var searchParam = metadata.rest[0].resource.filter(r => r.type === resourceType[0])[0].searchParam;
				//Resource
				if (resourceType[0] !== undefined && resourceType[1] === undefined && resourceType[2] === undefined && resourceType[3] === undefined) {
					createHelp(searchParam);
				}
				//Resource/_history
				else if (resourceType[0] !== undefined && resourceType[1] === undefined && resourceType[2] !== undefined && resourceType[3] === undefined) {
					createHelp(searchParam.filter(p => ['_count', '_format', '_page', '_pretty'].includes(p.name)));
				}
				//Resource/id
				else if (resourceType[0] !== undefined && resourceType[1] !== undefined && resourceType[2] === undefined && resourceType[3] === undefined) {
					createHelp(searchParam);
				}
				//Resource/id/_history
				else if (resourceType[0] !== undefined && resourceType[1] !== undefined && resourceType[2] !== undefined && resourceType[3] === undefined) {
					createHelp(searchParam.filter(p => ['_count', '_format', '_page', '_pretty'].includes(p.name)));
				}
				//Resource/id/_history/version
				else if (resourceType[0] !== undefined && resourceType[1] !== undefined && resourceType[2] !== undefined && resourceType[3] !== undefined) {
					createHelp(searchParam);
				}
			}
		}
	}

	var help = document.getElementById('help');
	help.style.display = 'block';
}

function getResourceTypeForCurrentUrl() {
	var url = window.location.pathname;
	var regex = new RegExp('(([A-Za-z0-9\-\\\.\:\%\$]*\/)+)?'
		+ '(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet|VerificationResult|VisionPrescription)'
		+ '(\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})?(\/_history)?(\/[0-9]+)?.*');
	var match = regex.exec(url);
	if (match != null && match.length == 7)
		return [match[3], match[4], match[5], match[6]];
	else
		return null;
}

function createHelp(searchParam) {
	var helpList = document.getElementById('help-list');
	helpList.innerHTML = null;

	searchParam.forEach(param => {
		var div = document.createElement("div");
		var span1 = document.createElement("span");
		var span2 = document.createElement("span");
		var p = document.createElement("p");

		div.appendChild(span1);
		div.appendChild(span2);
		div.appendChild(p);
		helpList.appendChild(div);

		div.setAttribute('class', 'help-param');
		span1.innerText = param.name;
		span1.setAttribute('class', 'help-param-name');
		span2.innerText = param.type;
		span2.setAttribute('class', 'help-param-type');
		p.innerText = param.documentation;
		p.setAttribute('class', 'help-param-documentation');
	});
}