function openTab(lang) {
    const tabcontent = document.getElementsByClassName("prettyprint");
    for (let i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    const tablinks = document.getElementsByClassName("tablinks");
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    document.getElementById(lang).style.display = "block";
    document.getElementById(lang + "-button").className += " active";

    if (lang != "html" && localStorage != null)
        localStorage.setItem('lang', lang);
    
    if (lang == "html")
        lang = localStorage != null && localStorage.getItem("lang") != null ? localStorage.getItem("lang") : "xml";

    setDownloadLink(lang);
}

function openInitialTab(htmlEnabled) {
    if (htmlEnabled)
        openTab("html");
    else {
        const lang = localStorage != null && localStorage.getItem("lang") != null ? localStorage.getItem("lang") : "xml";
        if (lang == "xml" || lang == "json")
        openTab(lang);
    }
}

function setDownloadLink(lang) {
    const searchParams = new URLSearchParams(document.location.search);
    searchParams.set('_format', lang);
    searchParams.set('_pretty', 'true');

    const downloadLink = document.getElementById('download-link');
    downloadLink.href = '?' + searchParams.toString();
    downloadLink.download = getDownloadFileName(lang);
    downloadLink.title = 'Download as ' + lang.toUpperCase();
}

function getDownloadFileName(lang) {
    const resourceType = getResourceTypeForCurrentUrl();

    /* /, /metadata, /_history */
    if (resourceType == null) {
        if (window.location.pathname.endsWith('/metadata')) {
            return "metadata." + lang;
        } else if (window.location.pathname.endsWith('/_history')) {
            return "history." + lang;
        } else {
            return "root." + lang;
        }
    } else {
        //Resource
        if (resourceType[1] !== undefined && resourceType[2] === undefined && resourceType[3] === undefined && resourceType[4] === undefined) {
            return resourceType[1] + '_Search.' + lang;
        }
        //Resource/_history
        else if (resourceType[1] !== undefined && resourceType[2] === undefined && resourceType[3] !== undefined && resourceType[4] === undefined) {
            return resourceType[1] + '_History.' + lang;
        }
        //Resource/id
        else if (resourceType[1] !== undefined && resourceType[2] !== undefined && resourceType[3] === undefined && resourceType[4] === undefined) {
            return resourceType[1] + '_' + resourceType[2].replace('/', '') + '.' + lang;
        }
        //Resource/id/_history
        else if (resourceType[1] !== undefined && resourceType[2] !== undefined && resourceType[3] !== undefined && resourceType[4] === undefined) {
            return resourceType[1] + '_' + resourceType[2].replace('/', '') + '_History.' + lang;
        }
        //Resource/id/_history/version
        else if (resourceType[1] !== undefined && resourceType[2] !== undefined && resourceType[3] !== undefined && resourceType[4] !== undefined) {
            return resourceType[1] + '_' + resourceType[2].replace('/', '') + '_v' + resourceType[4].replace('/', '') + '.' + lang;
        }
    }
}

function getResourceTypeForCurrentUrl() {
    const url = window.location.pathname;
    const regex = new RegExp('(?:(?:[A-Za-z0-9\-\\\.\:\%\$]*\/)+)?'
        + '(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BiologicallyDerivedProduct|BodyStructure|Bundle|CapabilityStatement|CarePlan|CareTeam|CatalogEntry|ChargeItem|ChargeItemDefinition|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|CoverageEligibilityRequest|CoverageEligibilityResponse|DetectedIssue|Device|DeviceDefinition|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EffectEvidenceSynthesis|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|EventDefinition|Evidence|EvidenceVariable|ExampleScenario|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingStudy|Immunization|ImmunizationEvaluation|ImmunizationRecommendation|ImplementationGuide|InsurancePlan|Invoice|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationKnowledge|MedicationRequest|MedicationStatement|MedicinalProduct|MedicinalProductAuthorization|MedicinalProductContraindication|MedicinalProductIndication|MedicinalProductIngredient|MedicinalProductInteraction|MedicinalProductManufactured|MedicinalProductPackaged|MedicinalProductPharmaceutical|MedicinalProductUndesirableEffect|MessageDefinition|MessageHeader|MolecularSequence|NamingSystem|NutritionOrder|Observation|ObservationDefinition|OperationDefinition|OperationOutcome|Organization|OrganizationAffiliation|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|Provenance|Questionnaire|QuestionnaireResponse|RelatedPerson|RequestGroup|ResearchDefinition|ResearchElementDefinition|ResearchStudy|ResearchSubject|RiskAssessment|RiskEvidenceSynthesis|Schedule|SearchParameter|ServiceRequest|Slot|Specimen|SpecimenDefinition|StructureDefinition|StructureMap|Subscription|Substance|SubstanceNucleicAcid|SubstancePolymer|SubstanceProtein|SubstanceReferenceInformation|SubstanceSourceMaterial|SubstanceSpecification|SupplyDelivery|SupplyRequest|Task|TerminologyCapabilities|TestReport|TestScript|ValueSet|VerificationResult|VisionPrescription)'
        + '(?:(?:\/([A-Za-z0-9\-\.]{1,64}))?(?:\/(_history)(?:\/([0-9]{1,64}))?)?)?(?:\\?.*)?$');
    const match = regex.exec(url);
    if (match != null)
        return match;
    else
        return null;
}