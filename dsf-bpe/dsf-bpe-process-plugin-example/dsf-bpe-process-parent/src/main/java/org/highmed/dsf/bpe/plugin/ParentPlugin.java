package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.ApplicationContext;

public class ParentPlugin extends AbstractProcessEnginePlugin
{
	private static final String PARENT_FILE = "parent.bpmn";

	public ParentPlugin(ApplicationContext context)
	{
		super(context);
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance parentProcess = readAndValidateModel("/" + PARENT_FILE);
		deploy(processEngine, PARENT_FILE, parentProcess);
	}
}
