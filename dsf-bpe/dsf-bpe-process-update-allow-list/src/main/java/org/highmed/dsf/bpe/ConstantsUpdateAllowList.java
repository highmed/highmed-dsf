package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_URI_BASE;
import static org.highmed.dsf.bpe.UpdateAllowListProcessPluginDefinition.VERSION;

public interface ConstantsUpdateAllowList
{
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST = "http://highmed.org/fhir/CodeSystem/update-allow-list";
	String CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST = "highmed_allow_list";

	String DOWNLOAD_ALLOW_LIST_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-download-allow-list";
	String DOWNLOAD_ALLOW_LIST_PROCESS_URI = PROCESS_URI_BASE + "downloadAllowList/";
	String DOWNLOAD_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION = DOWNLOAD_ALLOW_LIST_PROCESS_URI + VERSION;
	String DOWNLOAD_ALLOW_LIST_MESSAGE_NAME = "downloadAllowListMessage";

	String UPDATE_ALLOW_LIST_TASK_PROFILE = "http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list";
	String UPDATE_ALLOW_LIST_PROCESS_URI = PROCESS_URI_BASE + "updateAllowList/";
	String UPDATE_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION = UPDATE_ALLOW_LIST_PROCESS_URI + VERSION;
	String UPDATE_ALLOW_LIST_MESSAGE_NAME = "updateAllowListMessage";
}
