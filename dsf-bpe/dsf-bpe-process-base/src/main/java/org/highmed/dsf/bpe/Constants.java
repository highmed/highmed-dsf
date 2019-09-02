package org.highmed.dsf.bpe;

public interface Constants
{
	String VARIABLE_MESSAGE_NAME = "messageName";
	String VARIABLE_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	String VARIABLE_VERSION_TAG = "versionTag";

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
	String VARIABLE_QUERY_PARAMETERS = "queryParameters";
	String VARIABLE_BUNDLE_ID = "bundleId";
	String VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT = "simpleCohortSizeQueryFinalResult";
	String VARIABLE_PROCESS_OUTPUTS = "outputs"; // stores Map<String, String>

	String CODESYSTEM_HIGHMED_BPMN = "http://highmed.org/fhir/CodeSystem/bpmn-message";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME = "message-name";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY = "correlation-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE = "bundle-reference";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE = "research-study-reference";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS = "endpoint-address";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE = "error";

	String NAMINGSYSTEM_HIGHMED_BPMN_COHORT_SIZE_QUERY_RESULT = "http://highmed.org/fhir/NamingSystem/cohort-size-result";
	String NAMINGSYSTEM_HIGHMED_BPMN_COHORT_SIZE_QUERY_RESULT_VALUE_PREFIX_SINGLE_RESULT = "singleResult|";
	String NAMINGSYSTEM_HIGHMED_BPMN_COHORT_SIZE_QUERY_RESULT_VALUE_PREFIX_PARTICIPATING_MEDICS = "participatinMedics|";
	String NAMINGSYSTEM_HIGHMED_BPMN_COHORT_SIZE_QUERY_RESULT_VALUE_PREFIX_COHORT_SIZE = "cohortSize|";

	String PROCESS_URI_BASE = "http://highmed.org/bpe/Process/";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/organization";
	String ENDPOINT_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/endpoint";

	String BUNDLE_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/bundle";
	String WHITE_LIST_BUNDLE_IDENTIFIER_VALUE = "HiGHmed_white_list";

	String EXTENSION_PARTICIPATING_MEDIC_URI = "http://highmed.org/fhir/StructureDefinition/participating-medic";
}
