package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.ApplicationContext;

public class UpdateWhiteListPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_WHITE_LISTE_FILE = "updateWhiteListe.bpmn";

	public UpdateWhiteListPlugin(ApplicationContext context)
	{
		super(context);
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateWhiteListeProcess = readAndValidateModel("/" + UPDATE_WHITE_LISTE_FILE);
		deploy(processEngine, UPDATE_WHITE_LISTE_FILE, updateWhiteListeProcess);
	}
}
