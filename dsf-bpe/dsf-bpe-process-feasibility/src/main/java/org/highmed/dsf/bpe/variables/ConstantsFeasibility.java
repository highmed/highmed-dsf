package org.highmed.dsf.bpe.variables;

public interface ConstantsFeasibility
{
	String VARIABLE_BLOOM_FILTER_CONFIG = "bloomFilterConfig";
	String VARIABLE_QUERY_RESULTS = "queryResults";
	String VARIABLE_FINAL_QUERY_RESULTS = "finalQueryResults";
	String VARIABLE_RESEARCH_STUDY = "researchStudy";
	String VARIABLE_COHORTS = "cohorts";
	String VARIABLE_QUERIES = "queries";
	String VARIABLE_NEEDS_CONSENT_CHECK = "needsConsentCheck";
	String VARIABLE_NEEDS_RECORD_LINKAGE = "needsRecordLinkage";

	String ERROR_CODE_MULTI_MEDIC_RESULT = "errorMultiMedicSimpleFeasibilityResult";

	String CODESYSTEM_HIGHMED_FEASIBILITY = "http://highmed.org/fhir/CodeSystem/feasibility";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY = "medic-correlation-key";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK = "needs-consent-check";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE = "needs-record-linkage";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG = "bloom-filter-configuration";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT = "single-medic-result";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE = "single-medic-result-reference";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT = "participating-medics";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NOT_ENOUGH_PARTICIPATION = "not-enough-participation";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT = "multi-medic-result";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE = "research-study-reference";

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;

	String SIMPLE_FEASIBILITY_QUERY_PREFIX = "select count";

	String EXTENSION_PARTICIPATING_MEDIC_URI = "http://highmed.org/fhir/StructureDefinition/participating-medic";
	String EXTENSION_PARTICIPATING_TTP_URI = "http://highmed.org/fhir/StructureDefinition/participating-ttp";
	String EXTENSION_GROUP_ID_URI = "http://highmed.org/fhir/StructureDefinition/group-id";

	String LOCAL_SERVICES_INTEGRATION_PROCESS_URI = "http://highmed.org/bpe/Process/localServicesIntegration";
}
