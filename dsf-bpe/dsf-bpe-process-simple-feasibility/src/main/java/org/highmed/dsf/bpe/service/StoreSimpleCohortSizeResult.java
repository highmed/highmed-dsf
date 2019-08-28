package org.highmed.dsf.bpe.service;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreSimpleCohortSizeResult extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreSimpleCohortSizeResult.class);

	public StoreSimpleCohortSizeResult(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		ArrayList<Integer> results = (ArrayList<Integer>) execution.getVariable(Constants.VARIABLE_COHORT_SIZE_RESULTS);
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		// TODO: change to multiinstance for multiple queries in research study
		Integer cohortSize = ((IntegerType) task.getInput().stream()
				.filter(p -> p.getType().getCoding().get(0).getCode()
						.equals(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_SIMPLE_COHORT_SIZE_QUERY_RESULT)).findFirst()
				.get().getValue()).getValue();
		results.add(cohortSize);

		// race conditions are not possible, since task are received sequentially over the websocket connection
		// to the FHIR endpoint.
		execution.setVariable(Constants.VARIABLE_COHORT_SIZE_RESULTS, results);
	}
}
