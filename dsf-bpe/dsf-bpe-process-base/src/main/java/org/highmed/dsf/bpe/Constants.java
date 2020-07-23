package org.highmed.dsf.bpe;

import org.hl7.fhir.r4.model.CodeType;

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
	String VARIABLE_BLOOM_FILTER_CONFIG = "bloomFilterConfig";
	String VARIABLE_QUERY_RESULTS = "queryResults";
	String VARIABLE_FINAL_QUERY_RESULTS = "finalQueryResults";
	String VARIABLE_TASK = "task";
	String VARIABLE_LEADING_TASK = "leadingTask";
	String VARIABLE_RESEARCH_STUDY = "researchStudy";
	String VARIABLE_COHORTS = "cohorts";
	String VARIABLE_TTP_IDENTIFIER = "ttp";
	String VARIABLE_QUERIES = "queries";
	String VARIABLE_QUERY_PARAMETERS = "queryParameters";
	String VARIABLE_BUNDLE_ID = "bundleId";
	String VARIABLE_NEEDS_CONSENT_CHECK = "needsConsentCheck";
	String VARIABLE_NEEDS_RECORD_LINKAGE = "needsRecordLinkage";

	/**
	 * Stores a List<{@link org.highmed.dsf.fhir.variables.Outputs}> </>of outputs that have to be written to a task
	 * resource after the process terminates. Do not override, only add new entries to the list.
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

	String CODESYSTEM_HIGHMED_UPDATE_WHITELIST = "http://highmed.org/fhir/CodeSystem/update-whitelist";
	String CODESYSTEM_HIGHMED_UPDATE_WHITELIST_VALUE_WHITE_LIST = "highmed_whitelist";

	String CODESYSTEM_QUERY_TYPE = "http://highmed.org/fhir/CodeSystem/query-type";
	String CODESYSTEM_QUERY_TYPE_AQL = "application/x-aql-query";
	CodeType AQL_QUERY_TYPE = new CodeType(CODESYSTEM_QUERY_TYPE_AQL)
			.setSystem(CODESYSTEM_QUERY_TYPE);

	String PROCESS_URI_BASE = "http://highmed.org/bpe/Process/";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/organization-identifier";
	String ORGANIZATION_TYPE_SYSTEM = "http://highmed.org/fhir/CodeSystem/organization-type";
	String ENDPOINT_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/endpoint-identifier";

	String EXTENSION_PARTICIPATING_MEDIC_URI = "http://highmed.org/fhir/StructureDefinition/participating-medic";
	String EXTENSION_PARTICIPATING_TTP_URI = "http://highmed.org/fhir/StructureDefinition/participating-ttp";
	String EXTENSION_QUERY_URI = "http://highmed.org/fhir/StructureDefinition/query";
	String EXTENSION_GROUP_ID_URI = "http://highmed.org/fhir/StructureDefinition/group-id";

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;

	String SIMPLE_FEASIBILITY_QUERY_PREFIX = "select count";

	String OPENEHR_MIMETYPE_JSON = "application/json";
}
