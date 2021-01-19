package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.UpdateResourcesProcessPluginDefinition.VERSION;

public interface ConstantsUpdateResources
{
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE = "http://highmed.org/fhir/CodeSystem/update-resources";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE = "bundle-reference";
	String CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER = "organization-identifier-search-parameter";

	String PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES = "http://highmed.org/fhir/StructureDefinition/task-request-update-resources";
	String PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI =
			PROCESS_HIGHMED_URI_BASE + "requestUpdateResources/";
	String PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_MESSAGE_NAME = "requestUpdateResourcesMessage";

	String PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES = "http://highmed.org/fhir/StructureDefinition/task-execute-update-resources";
	String PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_PROCESS_URI =
			PROCESS_HIGHMED_URI_BASE + "executeUpdateResources/";
	String PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_MESSAGE_NAME = "executeUpdateResourcesMessage";
}
