package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class LocalServicesPlugin extends AbstractProcessEnginePlugin
{
	private static final String LOCAL_SERVICE_INTEGRATION_FILE = "localServicesIntegration.bpmn";

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance requestProcess = readAndValidateModel("/" + LOCAL_SERVICE_INTEGRATION_FILE);
		deploy(processEngine, LOCAL_SERVICE_INTEGRATION_FILE, requestProcess);
	}
}
