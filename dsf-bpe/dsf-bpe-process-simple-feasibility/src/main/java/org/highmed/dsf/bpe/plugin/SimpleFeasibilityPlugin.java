package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class SimpleFeasibilityPlugin extends AbstractProcessEnginePlugin
{
	private static final String REQUEST_FILE = "requestSimpleCohortSizeQuery.bpmn";
	private static final String EXECUTION_FILE = "executeSimpleCohortSizeQuery.bpmn";

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
		BpmnModelInstance requestProcess = readAndValidateModel("/" + REQUEST_FILE);
		delpoy(processEngine, REQUEST_FILE, requestProcess);

		BpmnModelInstance executionProcess = readAndValidateModel("/" + EXECUTION_FILE);
		delpoy(processEngine, EXECUTION_FILE, executionProcess);
	}
}
