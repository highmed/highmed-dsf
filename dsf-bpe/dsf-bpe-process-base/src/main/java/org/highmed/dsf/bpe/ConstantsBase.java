package org.highmed.dsf.bpe;

import org.hl7.fhir.r4.model.CodeType;

public interface ConstantsBase
{
	String VARIABLE_MESSAGE_NAME = "messageName";
	String VARIABLE_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	String VARIABLE_VERSION_TAG = "versionTag";
	String VARIABLE_PROFILE = "profile";
	// String VARIABLE_TARGET_ORGANIZATION_ID = "targetOrganizationId";
	// String VARIABLE_CORRELATION_KEY = "correlationKey";
	String VARIABLE_TARGET = "target";
	String VARIABLE_TARGETS = "targets";
	String VARIABLE_TASK = "task";
	String VARIABLE_LEADING_TASK = "leadingTask";
	String VARIABLE_BUNDLE_ID = "bundleId";
	String VARIABLE_QUERY_PARAMETERS = "queryParameters";
	String VARIABLE_TTP_IDENTIFIER = "ttp";

	/**
	 * Used to distinguish if I am at the moment in a process called by another process by a CallActivity or not
	 */
	String VARIABLE_IN_CALLED_PROCESS = "inCalledProcess";

	String CODESYSTEM_HIGHMED_BPMN = "http://highmed.org/fhir/CodeSystem/bpmn-message";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME = "message-name";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY = "correlation-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE = "error";

	String PROCESS_URI_BASE = "http://highmed.org/bpe/Process/";

	String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/organization-identifier";
	String ORGANIZATION_TYPE_SYSTEM = "http://highmed.org/fhir/CodeSystem/organization-type";
	String ORGANIZATION_TYPE_TTP = "TTP";
	String ENDPOINT_IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/endpoint-identifier";

	String EXTENSION_QUERY_URI = "http://highmed.org/fhir/StructureDefinition/query";
	String CODESYSTEM_QUERY_TYPE = "http://highmed.org/fhir/CodeSystem/query-type";
	String CODESYSTEM_QUERY_TYPE_AQL = "application/x-aql-query";
	CodeType AQL_QUERY_TYPE = new CodeType(CODESYSTEM_QUERY_TYPE_AQL).setSystem(CODESYSTEM_QUERY_TYPE);

	String OPENEHR_MIMETYPE_JSON = "application/json";
}
