package org.highmed.dsf.bpe.delegate;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDelegate implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;

	private DelegateExecution execution;

	public AbstractServiceDelegate(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		this.clientProvider = clientProvider;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public final void execute(DelegateExecution execution) throws Exception
	{
		this.execution = execution;

		try
		{
			logger.trace("Execution of task with id='{}'", execution.getCurrentActivityId());

			doExecute(execution);
		}
		// Error boundary event, do not stop process execution
		catch (BpmnError error)
		{
			Task task = getTask(execution);

			logger.debug("Error while executing service delegate " + getClass().getName(), error);
			logger.error(
					"Process {} encountered error boundary event in step {} for task with id {}, error-code: {}, message: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					error.getErrorCode(), error.getMessage());

			throw error;
		}
		// Not an error boundary event, stop process execution
		catch (Exception exception)
		{
			Task task = getTask(execution);

			logger.debug("Error while executing service delegate " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task with id {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(taskHelper.createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
					ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));
			task.setStatus(Task.TaskStatus.FAILED);

			clientProvider.getLocalWebserviceClient().withMinimalReturn().update(task);

			// TODO evaluate throwing exception as alternative to stopping the process instance
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
	}

	private Task getTask(DelegateExecution execution)
	{
		return execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId())
				? getLeadingTaskFromExecutionVariables()
				: getCurrentTaskFromExecutionVariables();
	}

	/**
	 * Method called by a BPMN service task
	 *
	 * @param execution
	 *            Process instance information and variables
	 * @throws BpmnError
	 *             Thrown when a error boundary event should be called
	 * @throws Exception
	 *             Uncaught exceptions will result in task status failed, the exception message will be written as an
	 *             error output
	 */
	protected abstract void doExecute(DelegateExecution execution) throws BpmnError, Exception;

	protected final TaskHelper getTaskHelper()
	{
		return taskHelper;
	}

	protected final FhirWebserviceClientProvider getFhirWebserviceClientProvider()
	{
		return clientProvider;
	}

	/**
	 * @return the current task from execution variables, the task resource that started the current process or
	 *         subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#VARIABLE_TASK
	 */
	protected final Task getCurrentTaskFromExecutionVariables()
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		return (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);
	}

	/**
	 * @return the leading task from execution variables, same as current task if not in a subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#VARIABLE_LEADING_TASK
	 */
	protected final Task getLeadingTaskFromExecutionVariables()
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Task leadingTask = (Task) execution.getVariable(ConstantsBase.VARIABLE_LEADING_TASK);
		return leadingTask != null ? leadingTask : getCurrentTaskFromExecutionVariables();
	}
}
