package org.highmed.dsf.bpe.variables;

public interface ConstantsUpdateAllowList
{
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST = "http://highmed.org/fhir/CodeSystem/update-allow-list";
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST = "highmed_allow_list";

	String DOWNLOAD_ALLOW_LIST_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-download-allow-list";
	String DOWNLOAD_ALLOW_LIST_PROCESS_URI = "http://highmed.org/bpe/Process/downloadAllowList";
	String DOWNLOAD_ALLOW_LIST_PROCESS_LATEST_VERSION = "0.4.0";
	String DOWNLOAD_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION =
			DOWNLOAD_ALLOW_LIST_PROCESS_URI + "/" + DOWNLOAD_ALLOW_LIST_PROCESS_LATEST_VERSION;

	String DOWNLOAD_ALLOW_LIST_MESSAGE_NAME = "downloadAllowListMessage";

	String UPDATE_ALLOW_LIST_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list";
	String UPDATE_ALLOW_LIST_PROCESS_URI = "http://highmed.org/bpe/Process/updateAllowList";
	String UPDATE_ALLOW_LIST_PROCESS_LATEST_VERSION = "0.4.0";
	String UPDATE_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION =
			UPDATE_ALLOW_LIST_PROCESS_URI + "/" + UPDATE_ALLOW_LIST_PROCESS_LATEST_VERSION;

	String UPDATE_ALLOW_LIST_MESSAGE_NAME = "updateAllowListMessage";
}
