package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class UpdateWhiteListPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_WHITE_LISTE_FILE = "updateWhiteListe.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateWhiteListeProcess = readAndValidateModel("/" + UPDATE_WHITE_LISTE_FILE);
		deploy(processEngine, UPDATE_WHITE_LISTE_FILE, updateWhiteListeProcess);
	}
}
