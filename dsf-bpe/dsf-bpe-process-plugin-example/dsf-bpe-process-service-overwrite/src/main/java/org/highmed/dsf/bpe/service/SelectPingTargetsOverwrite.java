package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectPingTargetsOverwrite extends SelectPingTargets
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargetsOverwrite.class);

	public SelectPingTargetsOverwrite(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper, organizationProvider);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		logger.info("--- THIS OVERWRITES SelectPingTargets AND STOPS THE PROCESS EXECUTION ---");

		execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
				"Only for demonstration of SelectPingTargets overwrite with plugin");
	}
}
