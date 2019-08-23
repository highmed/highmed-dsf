package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;

public class UpdateWhiteListPlugin extends AbstractProcessEnginePlugin
{
	private static final String UPDATE_WHITE_LISTE_FILE = "updateWhiteListe.bpmn";

	public UpdateWhiteListPlugin(DefaultBpmnParseListener defaultBpmnParseListener) {
		super(defaultBpmnParseListener);
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance updateWhiteListeProcess = readAndValidateModel("/" + UPDATE_WHITE_LISTE_FILE);
		delpoy(processEngine, UPDATE_WHITE_LISTE_FILE, updateWhiteListeProcess);
	}
}
