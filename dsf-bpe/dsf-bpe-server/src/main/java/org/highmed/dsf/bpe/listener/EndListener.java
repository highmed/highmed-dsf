package org.highmed.dsf.bpe.listener;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Output;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;

/**
 * Added to each BPMN EndEvent by the {@link DefaultBpmnParseListener}.
 * Is used to set the FHIR {@link Task} status as {@link Task.TaskStatus#COMPLETED} if the process ends successfully
 * and sets {@link Task}.output values. Sets the {@link Constants#VARIABLE_IN_CALLED_PROCESS} back to <code>false</code>
 * if a called sub process ends.
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
		boolean inCalledProcess = (boolean) execution.getVariable(Constants.VARIABLE_IN_CALLED_PROCESS);

		// not in a called process --> process end if it is not a subprocess
		if (!inCalledProcess)
		{
			Task task;
			if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
			{
				// not in a subprocess --> end of main process, write process outputs to task
				task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);

				Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
				task = taskHelper.addOutputs(task, outputs);
			}
			else
			{
				// in a subprocess --> process does not end here, outputs do not have to be written
				task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			}

			task.setStatus(Task.TaskStatus.COMPLETED);
			webserviceClient.update(task);
		}
		else
		{
			// in a called process --> process does not end here, don't change the task variable
			// reset VARIABLE_IS_CALL_ACTIVITY to false, since we leave the called process
			execution.setVariable(Constants.VARIABLE_IN_CALLED_PROCESS, false);
		}
	}
}