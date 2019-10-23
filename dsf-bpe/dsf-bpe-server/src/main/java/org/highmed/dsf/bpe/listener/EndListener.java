package org.highmed.dsf.bpe.listener;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;

/**
 * Added to each EndEvent by the {@link DefaultBpmnParseListener}.
 * Can be used to execute certain things before a (sub- or call-) process ends completely.
 */
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

		// not in a call activity --> process end if it is not a subprocess
		if (!isCallActivity)
		{
			Task task;
			if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
			{
				// not in a subprocess --> end of main process, write process outputs to task
				task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);

				@SuppressWarnings("unchecked")
				List<OutputWrapper> outputs = (List<OutputWrapper>) execution
						.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
				task = taskHelper.addOutputs(task, outputs);
			}
			else
			{
				// in a subprocess --> process does not end here, outputs do not have to be written
				task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			}

			task.setStatus(Task.TaskStatus.COMPLETED);
			webserviceClient.update(task);
		} else {
			// in a call activity --> process does not end here, don't change the task variable
			// reset VARIABLE_IS_CALL_ACTIVITY to false, since we leave the called process
			execution.setVariable(Constants.VARIABLE_IS_CALL_ACTIVITY, false);
		}
	}
}