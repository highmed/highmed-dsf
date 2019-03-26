package org.highmed.bpe;

public interface Constants
{
	String VARIABLE_MESSAGE_NAME = "messageName";
	String VARIABLE_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	String VARIABLE_VERSION_TAG = "versionTag";
	String VARIABLE_TARGET_ORGANIZATION = "targetOrganization";
	String VARIABLE_CORRELATION_KEY = "correlationKey";
	String VARIABLE_TASK = "task";

	String CODESYSTEM_HIGHMED_BPMN = "http://highmed.org/fhir/CodeSystem/bpmn-message";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME = "message-name";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY = "business-key";
	String CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY = "correlation-key";

	String PROCESS_URI_BASE = "http://highmed.org/bpe/Process/";
}
