package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class UpdateWhitelistPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_WHITELIST_FILE = "updateWhitelist.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateWhiteListeProcess = readAndValidateModel("/" + UPDATE_WHITELIST_FILE);
		deploy(processEngine, UPDATE_WHITELIST_FILE, updateWhiteListeProcess);
	}
}
