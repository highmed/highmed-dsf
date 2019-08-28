package org.highmed.dsf.bpe.service;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckMultiMedicSimpleCohortSizeResult extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckMultiMedicSimpleCohortSizeResult.class);

	public CheckMultiMedicSimpleCohortSizeResult(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Integer result = (Integer) execution.getVariable(Constants.VARIABLE_MULTI_MEDIC_COHORT_SIZE_RESULT);
		Integer medics = (Integer) execution.getVariable(Constants.VARIABLE_MULTI_MEDIC_PARTICIPATION_RESULT);

		// TODO: implement check for result
		// TODO: implement check for ...

		@SuppressWarnings("unchecked")
		Map<String, String> outputs = (Map<String, String>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		outputs.put(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_SIMPLE_COHORT_SIZE_QUERY_RESULT, String.valueOf(result));
		outputs.put(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_PARTICIPATION_RESULT, String.valueOf(medics));
		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private void stopInstance(String reason)
	{
		logger.error("Result review failed, reason {}", reason);
		throw new RuntimeException("Result review failed, reason " + reason);
	}
}
