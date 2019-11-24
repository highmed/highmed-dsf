package org.highmed.dsf.bpe.listener;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Added to each BPMN StartEvent by the {@link DefaultBpmnParseListener}. Initializes the
 * {@link Constants#VARIABLE_IN_CALLED_PROCESS} variable with <code>false</code> for processes started via a
 * {@link Task} resource.
 */
public class StartListener implements ExecutionListener
{
	private static final Logger logger = LoggerFactory.getLogger(StartListener.class);

	private TaskHelper taskHelper;

	public StartListener(TaskHelper taskHelper)
	{
		this.taskHelper = Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		// Task.status.INPROGRESS is set in the TaskHandler when the task is received
		// start of main process instance if no parent available or the parent id is same as the actual process id
		if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
		{
			Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

			// sets initial task variable a second time i a different variable. subprocesses which start
			// with a task resource override the initially set task variable
			execution.setVariable(Constants.VARIABLE_LEADING_TASK, task);

			// initialized process outputs variable, used in the EndListener
			execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(new Outputs()));

			log(execution, task);
		}

		// if a main process is started (not a call- or subprocess), this variable has to be initialized.
		// it is set to false, since a main process is not a called process
		if (!execution.getVariableNames().contains(Constants.VARIABLE_IN_CALLED_PROCESS))
		{
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

		logger.info("Starting process {} [message: {}, businessKey: {}, correlationKey: {}, taskId: {}]", processUrl,
				messageName, businessKey, correlationKey, taskId);
	}
}
