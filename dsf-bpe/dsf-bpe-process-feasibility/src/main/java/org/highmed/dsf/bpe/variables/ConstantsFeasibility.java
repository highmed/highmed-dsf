package org.highmed.dsf.bpe.variables;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_URI_BASE;

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

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;
	String SIMPLE_FEASIBILITY_QUERY_PREFIX = "select count";

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

	String EXTENSION_PARTICIPATING_MEDIC_URI = "http://highmed.org/fhir/StructureDefinition/participating-medic";
	String EXTENSION_PARTICIPATING_TTP_URI = "http://highmed.org/fhir/StructureDefinition/participating-ttp";
	String EXTENSION_GROUP_ID_URI = "http://highmed.org/fhir/StructureDefinition/group-id";

	String GROUP_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-group";
	String FEASIBILITY_RESEARCH_STUDY_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-research-study-feasibility";
	String RESEARCH_STUDY_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/research-study-identifier";

	String REQUEST_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility";
	String REQUEST_FEASIBILITY_PROCESS_URI = PROCESS_URI_BASE + "requestSimpleFeasibility/";
	String REQUEST_FEASIBILITY_PROCESS_LATEST_VERSION = "0.4.0";
	String REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			REQUEST_FEASIBILITY_PROCESS_URI + REQUEST_FEASIBILITY_PROCESS_LATEST_VERSION;
	String REQUEST_FEASIBILITY_MESSAGE_NAME = "requestSimpleFeasibilityMessage";

	String EXECUTE_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-execute-simple-feasibility";
	String EXECUTE_FEASIBILITY_PROCESS_URI = PROCESS_URI_BASE + "executeSimpleFeasibility/";
	String EXECUTE_FEASIBILITY_PROCESS_LATEST_VERSION = "0.4.0";
	String EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			EXECUTE_FEASIBILITY_PROCESS_URI + EXECUTE_FEASIBILITY_PROCESS_LATEST_VERSION;
	String EXECUTE_FEASIBILITY_MESSAGE_NAME = "executeSimpleFeasibilityMessage";

	String COMPUTE_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-compute-simple-feasibility";
	String COMPUTE_FEASIBILITY_PROCESS_URI = PROCESS_URI_BASE + "computeSimpleFeasibility/";
	String COMPUTE_FEASIBILITY_PROCESS_LATEST_VERSION = "0.4.0";
	String COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			COMPUTE_FEASIBILITY_PROCESS_URI + COMPUTE_FEASIBILITY_PROCESS_LATEST_VERSION;
	String COMPUTE_FEASIBILITY_MESSAGE_NAME = "computeSimpleFeasibilityMessage";

	String SINGLE_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-single-medic-result-simple-feasibility";
	String SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME = "resultSingleMedicSimpleFeasibilityMessage";

	String MULTI_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-multi-medic-result-simple-feasibility";
	String MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME = "resultMultiMedicSimpleFeasibilityMessage";

	String ERROR_FEASIBILITY_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-multi-medic-error-simple-feasibility";
	String ERROR_FEASIBILITY_MESSAGE_NAME = "errorMultiMedicSimpleFeasibilityMessage";

	String LOCAL_SERVICES_PROCESS_URI = PROCESS_URI_BASE + "localServicesIntegration/";
}
