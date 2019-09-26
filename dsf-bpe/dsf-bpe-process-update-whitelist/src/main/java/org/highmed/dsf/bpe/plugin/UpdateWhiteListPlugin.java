package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.highmed.dsf.bpe.plugin.AbstractProcessEnginePlugin;

public class UpdateWhiteListPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_WHITE_LISTE_FILE = "updateWhiteListe.bpmn";

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateWhiteListeProcess = readAndValidateModel("/" + UPDATE_WHITE_LISTE_FILE);
		deploy(processEngine, UPDATE_WHITE_LISTE_FILE, updateWhiteListeProcess);
	}
}
