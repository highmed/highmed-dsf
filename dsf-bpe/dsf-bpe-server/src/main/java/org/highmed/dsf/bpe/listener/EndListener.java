package org.highmed.dsf.bpe.listener;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;

@SuppressWarnings("unchecked")
public class EndListener implements ExecutionListener
{
	private final TaskHelper taskHelper;
	private final FhirWebserviceClient webserviceClient;

	public EndListener(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		this.taskHelper = taskHelper;
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		boolean isCallActivity = (boolean) execution.getVariable(Constants.VARIABLE_IS_CALL_ACTIVITY);

		if (!isCallActivity)
		{
			Task task;
			if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
			{
				// not in a subprocess --> end of main process
				task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);

				List<OutputWrapper> outputs = (List<OutputWrapper>) execution
						.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
				task = taskHelper.addOutputs(task, outputs);
			}
			else
			{
				task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			}

			task.setStatus(Task.TaskStatus.COMPLETED);
			webserviceClient.update(task);
		} else {
			execution.setVariable(Constants.VARIABLE_IS_CALL_ACTIVITY, false);
		}
	}
}