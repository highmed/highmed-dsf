package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Task;

import java.util.Collections;
import java.util.HashMap;

public class StartListener implements ExecutionListener
{
	private WebserviceClient webserviceClient;

	public StartListener(WebserviceClient webserviceClient) {
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		// Task.status.INPROGRESS is set in the TaskHandler when the task is received
		// start of main process instance if no parent available
		if(execution.getParentId() == null) {
			Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			execution.setVariable(Constants.VARIABLE_LEADING_TASK, task);

			execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, new HashMap<>());
		}
	}
}
