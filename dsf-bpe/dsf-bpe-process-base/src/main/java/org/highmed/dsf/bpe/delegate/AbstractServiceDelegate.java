package org.highmed.dsf.bpe.delegate;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDelegate implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final FhirWebserviceClient webserviceClient;
	private final TaskHelper taskHelper;

	private DelegateExecution execution;
	private Task currentTask;
	private Task leadingTask;

	public AbstractServiceDelegate(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		this.clientProvider = clientProvider;
		Objects.requireNonNull(clientProvider, "clientProvider");

		this.webserviceClient = clientProvider.getLocalWebserviceClient();
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(webserviceClient, "webserviceClient");
		Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public final void execute(DelegateExecution execution) throws Exception
	{
		try
		{
			logger.trace("Execution of task with id='{}'", execution.getCurrentActivityId());

			setMembers(execution);

			doExecute(execution);
			doExecutePlugin(execution);
		}
		catch (Exception exception)
		{
			// Error boundary event, do not stop process execution
			if (exception instanceof BpmnError)
				throw exception;

			// Not an error boundary event, stop process execution
			Task task = execution.getParentId() == null || execution.getParentId()
					.equals(execution.getProcessInstanceId()) ? leadingTask : currentTask;

			logger.debug("Error while executing service delegate " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task with id {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(taskHelper.createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
					ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));

			task.setStatus(Task.TaskStatus.FAILED);
			webserviceClient.withMinimalReturn().update(task);

			execution.getProcessEngine().getRuntimeService()
					.deleteProcessInstance(execution.getProcessInstanceId(), exception.getMessage());
		}
	}

	private void doExecutePlugin(DelegateExecution execution)
	{
		// TODO: implement plugin system for individual checks in different medics, like:
		// - PI check
		// - Cohort characteristics check
		// - Queries check
		// - Requester check
		// - ...
	}

	/**
	 * Method called by a BPMN service task
	 *
	 * @param execution holding the process instance information and variables
	 * @throws Exception reason why process instance has failed, exception message will be stored in process associated fhir
	 *                   task resource as output
	 */
	protected abstract void doExecute(DelegateExecution execution) throws Exception;

	protected final TaskHelper getTaskHelper()
	{
		return taskHelper;
	}

	protected final FhirWebserviceClientProvider getFhirWebserviceClientProvider()
	{
		return clientProvider;
	}

	protected final Task getCurrentTaskFromExecutionVariables()
	{
		return currentTask;
	}

	protected final void setCurrentTaskToExecutionVariables(Task currentTask)
	{
		this.currentTask = currentTask;
		execution.setVariable(ConstantsBase.VARIABLE_TASK, currentTask);
	}

	protected final Task getLeadingTaskFromExecutionVariables()
	{
		return leadingTask;
	}

	protected final void setLeadingTaskToExecutionVariables(Task leadingTask)
	{
		this.leadingTask = leadingTask;
		execution.setVariable(ConstantsBase.VARIABLE_LEADING_TASK, leadingTask);
	}

	private void setMembers(DelegateExecution execution)
	{
		this.execution = execution;
		currentTask = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);
		leadingTask = (Task) execution.getVariable(ConstantsBase.VARIABLE_LEADING_TASK);
	}
}
