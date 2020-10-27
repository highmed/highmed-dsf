package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class UpdateAllowListPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_ALLOW_LIST_FILE = "updateAllowList.bpmn";
	private static final String DOWNLOAD_ALLOW_LIST_FILE = "downloadAllowList.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateAllowListProcess = readAndValidateModel("/" + UPDATE_ALLOW_LIST_FILE);
		deploy(processEngine, UPDATE_ALLOW_LIST_FILE, updateAllowListProcess);

		BpmnModelInstance downloadAllowListProcess = readAndValidateModel("/" + DOWNLOAD_ALLOW_LIST_FILE);
		deploy(processEngine, DOWNLOAD_ALLOW_LIST_FILE, downloadAllowListProcess);
	}
}
