package org.highmed.dsf.bpe;

public interface Constants
{
	String VARIABLE_MESSAGE_NAME = "messageName";
	String VARIABLE_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	String VARIABLE_VERSION_TAG = "versionTag";
	String VARIABLE_PROFILE = "profile";
	// String VARIABLE_TARGET_ORGANIZATION_ID = "targetOrganizationId";
	// String VARIABLE_CORRELATION_KEY = "correlationKey";
	String VARIABLE_MULTI_INSTANCE_TARGET = "multiInstanceTarget";
	String VARIABLE_MULTI_INSTANCE_TARGETS = "multiInstanceTargets";
	String VARIABLE_MULTI_INSTANCE_RESULT = "mutliInstanceResult";
	String VARIABLE_MULTI_INSTANCE_RESULTS = "mutliInstanceResults";
	String VARIABLE_TASK = "task";
	String VARIABLE_LEADING_TASK = "leadingTask";
	String VARIABLE_RESEARCH_STUDY = "researchStudy";
	String VARIABLE_COHORTS = "cohorts";
	String VARIABLE_QUERIES = "queries";
	String VARIABLE_QUERY_PARAMETERS = "queryParameters";
	String VARIABLE_BUNDLE_ID = "bundleId";

	/**
	 * Stores a List<{@link org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult}> of the final results of
	 * a multi medic simple cohort size query. Do not override, only add new entries to the list.
	 */
	String VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT = "simpleCohortSizeQueryFinalResult";

	/**
	 * Stores a List<{@link org.highmed.dsf.fhir.variables.Outputs}> </>of outputs that have to be written to a
	 * task resource after the process terminates. Do not override, only add new entries to the list.
	 */
	String VARIABLE_PROCESS_OUTPUTS = "outputs";

	/**
	 * Used to distinguish if I am at the moment in a process called by another process by a CallActivity or not
	 */
	String VARIABLE_IN_CALLED_PROCESS = "inCalledProcess";

	String CODESYSTEM_HIGHMED_BPMN = "http://highmed.org/fhir/CodeSystem/bpmn-message";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME = "message-name";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY = "correlation-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE = "error";

	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE = "http://highmed.org/fhir/CodeSystem/update-resources";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE = "bundle-reference";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER = "organization-identifier-search-parameter";

	String CODESYSTEM_HIGHMED_FEASIBILITY = "http://highmed.org/fhir/CodeSystem/feasibility";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT = "single-medic-result";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS = "participating-medics";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT = "multi-medic-result";
	String CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE = "research-study-reference";

	String CODESYSTEM_HIGHMED_UPDATE_WHITELIST = "http://highmed.org/fhir/CodeSystem/update-whitelist";
	String CODESYSTEM_HIGHMED_UPDATE_WHITELIST_VALUE_WHITE_LIST = "HiGHmed_white_list";

	String CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR = "|";
	int CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_COHORT_SIZE_INDEX = 0;
	int CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_GROUP_ID_INDEX = 1;

	String PROCESS_URI_BASE = "http://highmed.org/bpe/Process/";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/organization";
	String ENDPOINT_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/endpoint";

	String EXTENSION_PARTICIPATING_MEDIC_URI = "http://highmed.org/fhir/StructureDefinition/participating-medic";
	String EXTENSION_QUERY_URI = "http://highmed.org/fhir/StructureDefinition/query";

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;

	String SIMPLE_FEASIBILITY_QUERY_PREFIX = "select count";
}
