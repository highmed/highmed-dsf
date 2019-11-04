package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class ChildPlugin extends AbstractProcessEnginePlugin
{
	private static final String CHILD_FILE = "child.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance childProcess = readAndValidateModel("/" + CHILD_FILE);
		deploy(processEngine, CHILD_FILE, childProcess);
	}
}
