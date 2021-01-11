package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_URI_BASE;
import static org.highmed.dsf.bpe.UpdateResourcesProcessPluginDefinition.VERSION;

public interface ConstantsUpdateResources
{
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE = "http://highmed.org/fhir/CodeSystem/update-resources";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE = "bundle-reference";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER = "organization-identifier-search-parameter";

	String REQUEST_UPDATE_RESOURCES_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-request-update-resources";
	String REQUEST_UPDATE_RESOURCES_PROCESS_URI = PROCESS_URI_BASE + "requestUpdateResources/";
	String REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION = REQUEST_UPDATE_RESOURCES_PROCESS_URI + VERSION;
	String REQUEST_UPDATE_RESOURCES_MESSAGE_NAME = "requestUpdateResourcesMessage";

	String EXECUTE_UPDATE_RESOURCES_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-execute-update-resources";
	String EXECUTE_UPDATE_RESOURCES_PROCESS_URI = PROCESS_URI_BASE + "executeUpdateResources/";
	String EXECUTE_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION = EXECUTE_UPDATE_RESOURCES_PROCESS_URI + VERSION;
	String EXECUTE_UPDATE_RESOURCES_MESSAGE_NAME = "executeUpdateResourcesMessage";
}
