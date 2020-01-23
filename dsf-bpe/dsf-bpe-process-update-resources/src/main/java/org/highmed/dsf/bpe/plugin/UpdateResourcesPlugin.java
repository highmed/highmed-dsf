package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class UpdateResourcesPlugin extends AbstractProcessEnginePlugin
{
	private static final String EXECUTE_UPDATE_RESOURCES_FILE = "executeUpdateResources.bpmn";
	private static final String REQUEST_UPDATE_RESOURCES_FILE = "requestUpdateResources.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance executeUpdateResourcesProcess = readAndValidateModel("/" + EXECUTE_UPDATE_RESOURCES_FILE);
		deploy(processEngine, EXECUTE_UPDATE_RESOURCES_FILE, executeUpdateResourcesProcess);

		BpmnModelInstance requestUpdateResourcesProcess = readAndValidateModel("/" + REQUEST_UPDATE_RESOURCES_FILE);
		deploy(processEngine, REQUEST_UPDATE_RESOURCES_FILE, requestUpdateResourcesProcess);
	}
}
