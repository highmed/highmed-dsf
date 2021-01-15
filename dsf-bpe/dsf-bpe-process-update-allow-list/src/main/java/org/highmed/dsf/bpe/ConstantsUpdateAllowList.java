package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.UpdateAllowListProcessPluginDefinition.VERSION;

public interface ConstantsUpdateAllowList
{
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST = "http://highmed.org/fhir/CodeSystem/update-allow-list";
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST = "highmed_allow_list";

	String PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST = "http://highmed.org/fhir/StructureDefinition/task-download-allow-list";
	String PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "downloadAllowList/";
	String PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_MESSAGE_NAME = "downloadAllowListMessage";

	String PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST = "http://highmed.org/fhir/StructureDefinition/task-update-allow-list";
	String PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "updateAllowList/";
	String PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION =
			PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_MESSAGE_NAME = "updateAllowListMessage";
}
