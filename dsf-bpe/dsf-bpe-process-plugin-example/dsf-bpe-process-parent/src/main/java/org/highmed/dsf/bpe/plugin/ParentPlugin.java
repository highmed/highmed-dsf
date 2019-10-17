package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class ParentPlugin extends AbstractProcessEnginePlugin
{
	private static final String PARENT_FILE = "parent.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance parentProcess = readAndValidateModel("/" + PARENT_FILE);
		deploy(processEngine, PARENT_FILE, parentProcess);
	}
}
