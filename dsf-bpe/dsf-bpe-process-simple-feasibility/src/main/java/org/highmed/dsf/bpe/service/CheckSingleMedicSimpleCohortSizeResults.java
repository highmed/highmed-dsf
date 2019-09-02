package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.fhir.client.WebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSingleMedicSimpleCohortSizeResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSingleMedicSimpleCohortSizeResults.class);

	public CheckSingleMedicSimpleCohortSizeResults(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		MultiInstanceResult result = (MultiInstanceResult) execution.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT);

		doExecutePlugin(execution);
	}

	private void doExecutePlugin(DelegateExecution execution) {
		// TODO: implement plugin system for individual checks in different medics, like:
		// TODO:   - results check
		// TODO:   - ...
	}
}
