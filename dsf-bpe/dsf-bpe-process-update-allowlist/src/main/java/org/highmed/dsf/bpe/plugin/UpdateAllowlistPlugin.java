package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class UpdateAllowlistPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_ALLOWLIST_FILE = "updateAllowlist.bpmn";
	private static final String DOWNLOAD_ALLOWLIST_FILE = "downloadAllowlist.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateAllowlistProcess = readAndValidateModel("/" + UPDATE_ALLOWLIST_FILE);
		deploy(processEngine, UPDATE_ALLOWLIST_FILE, updateAllowlistProcess);

		BpmnModelInstance downloadAllowlistProcess = readAndValidateModel("/" + DOWNLOAD_ALLOWLIST_FILE);
		deploy(processEngine, DOWNLOAD_ALLOWLIST_FILE, downloadAllowlistProcess);
	}
}
