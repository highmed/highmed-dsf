package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.FeasibilityProcessPluginDefinition.VERSION;

public interface ConstantsFeasibility
{
	String BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY = "researchStudy";
	String BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK = "needsConsentCheck";
	String BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE = "needsRecordLinkage";
	String BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG = "bloomFilterConfig";
	String BPMN_EXECUTION_VARIABLE_COHORTS = "cohorts";
	String BPMN_EXECUTION_VARIABLE_QUERIES = "queries";
	String BPMN_EXECUTION_VARIABLE_QUERY_RESULTS = "queryResults";
	String BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS = "finalQueryResults";

	String BPMN_EXECUTION_ERROR_CODE_MULTI_MEDIC_RESULT = "errorMultiMedicFeasibilityResult";

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;
	String FEASIBILITY_QUERY_PREFIX = "select count";

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

	String EXTENSION_HIGHMED_PARTICIPATING_MEDIC = "http://highmed.org/fhir/StructureDefinition/extension-participating-medic";
	String EXTENSION_HIGHMED_PARTICIPATING_TTP = "http://highmed.org/fhir/StructureDefinition/extension-participating-ttp";
	String EXTENSION_HIGHMED_GROUP_ID = "http://highmed.org/fhir/StructureDefinition/extension-group-id";

	String PROFILE_HIGHMED_GROUP = "http://highmed.org/fhir/StructureDefinition/group";
	String PROFILE_HIGHEMD_RESEARCH_STUDY_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/research-study-feasibility";

	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-request-feasibility";
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "requestFeasibility/";
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MESSAGE_NAME = "requestFeasibilityMessage";

	String PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "localServicesIntegration/";

	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-execute-feasibility";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "executeFeasibility/";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MESSAGE_NAME = "executeFeasibilityMessage";

	String PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-compute-feasibility";
	String PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "computeFeasibility/";
	String PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_MESSAGE_NAME = "computeFeasibilityMessage";

	String PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-single-medic-result-feasibility";
	String PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME = "resultSingleMedicFeasibilityMessage";

	String PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-multi-medic-result-feasibility";
	String PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME = "resultMultiMedicFeasibilityMessage";

	String PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY = "http://highmed.org/fhir/StructureDefinition/task-multi-medic-error-feasibility";
	String PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY_MESSAGE_NAME = "errorMultiMedicFeasibilityMessage";

	String NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER = "http://highmed.org/fhir/NamingSystem/research-study-identifier";
}
