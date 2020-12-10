package org.highmed.dsf.bpe.variables;

public interface ConstantsUpdateResources
{
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE = "http://highmed.org/fhir/CodeSystem/update-resources";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE = "bundle-reference";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER = "organization-identifier-search-parameter";

	String REQUEST_UPDATE_RESOURCES_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-request-update-resources";
	String REQUEST_UPDATE_RESOURCES_PROCESS_URI = "http://highmed.org/bpe/Process/requestUpdateResources";
	String REQUEST_UPDATE_RESOURCES_PROCESS_LATEST_VERSION = "0.4.0";
	String REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION =
			REQUEST_UPDATE_RESOURCES_PROCESS_URI + "/" + REQUEST_UPDATE_RESOURCES_PROCESS_LATEST_VERSION;

	String REQUEST_UPDATE_RESOURCES_MESSAGE_NAME = "requestUpdateResourcesMessage";
}
