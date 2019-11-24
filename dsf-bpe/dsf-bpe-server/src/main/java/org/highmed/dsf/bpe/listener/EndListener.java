package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Added to each BPMN EndEvent by the {@link DefaultBpmnParseListener}. Is used to set the FHIR {@link Task} status as
 * {@link Task.TaskStatus#COMPLETED} if the process ends successfully and sets {@link Task}.output values. Sets the
 * {@link Constants#VARIABLE_IN_CALLED_PROCESS} back to <code>false</code> if a called sub process ends.
 */
public class EndListener implements ExecutionListener
{
	private static final Logger logger = LoggerFactory.getLogger(EndListener.class);

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

				log(execution, task);
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

	private void log(DelegateExecution execution, Task task)
	{
		String processUrl = task.getInstantiatesUri();
		String messageName = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME).orElse(null);
		String businessKey = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);
		String taskId = task.getIdElement().getIdPart();

		logger.info("Process {} finished [message: {}, businessKey: {}, correlationKey: {}, taskId: {}]", processUrl,
				messageName, businessKey, correlationKey, taskId);
	}
}