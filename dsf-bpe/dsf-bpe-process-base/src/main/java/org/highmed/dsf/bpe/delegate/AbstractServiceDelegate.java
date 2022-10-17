package org.highmed.dsf.bpe.delegate;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDelegate implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;
	private final ReadAccessHelper readAccessHelper;

	public AbstractServiceDelegate(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		this.clientProvider = clientProvider;
		this.taskHelper = taskHelper;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(taskHelper, "taskHelper");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public final void execute(DelegateExecution execution) throws Exception
	{
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
					"Process {} encountered error boundary event in step {} for task {}, error-code: {}, message: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), getTaskAbsoluteUrl(task),
					error.getErrorCode(), error.getMessage());

			throw error;
		}
		// Not an error boundary event, stop process execution
		catch (Exception exception)
		{
			Task task = getTask(execution);

			logger.debug("Error while executing service delegate " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task {}, reason: {} - {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), getTaskAbsoluteUrl(task),
					exception.getClass().getName(), exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(taskHelper.createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					errorMessage));
			task.setStatus(Task.TaskStatus.FAILED);

			clientProvider.getLocalWebserviceClient().withMinimalReturn().update(task);

			// TODO evaluate throwing exception as alternative to stopping the process instance
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
	}

	protected final String getTaskAbsoluteUrl(Task task)
	{
		return task == null ? "?"
				: task.getIdElement().toVersionless()
						.withServerBase(clientProvider.getLocalBaseUrl(), ResourceType.Task.name()).getValue();
	}

	/**
	 * Method called by a BPMN service task
	 *
	 * @param execution
	 *            Process instance information and variables
	 * @throws BpmnError
	 *             Thrown when an error boundary event should be called
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

	protected final ReadAccessHelper getReadAccessHelper()
	{
		return readAccessHelper;
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the active task from execution variables, i.e. the leading task if the main process is running or the
	 *         current task if a subprocess is running.
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getTask(DelegateExecution execution)
	{
		return taskHelper.getTask(execution);
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the current task from execution variables, the task resource that started the current process or
	 *         subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getCurrentTaskFromExecutionVariables(DelegateExecution execution)
	{
		return taskHelper.getCurrentTaskFromExecutionVariables(execution);
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the leading task from execution variables, same as current task if not in a subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final Task getLeadingTaskFromExecutionVariables(DelegateExecution execution)
	{
		return taskHelper.getLeadingTaskFromExecutionVariables(execution);
	}

	/**
	 * <i>Use this method to update the process engine variable {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK},
	 * after modifying the {@link Task}.</i>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final void updateCurrentTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		taskHelper.updateCurrentTaskInExecutionVariables(execution, task);
	}

	/**
	 * <i>Use this method to update the process engine variable
	 * {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK}, after modifying the {@link Task}.</i>
	 * <p>
	 * Updates the current task if no leading task is set.
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final void updateLeadingTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		taskHelper.updateLeadingTaskInExecutionVariables(execution, task);
	}
}
