package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TASK;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractDelegateAndListener implements InitializingBean
{
	private final FhirWebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;
	private final ReadAccessHelper readAccessHelper;

	private DelegateExecution execution;

	public AbstractDelegateAndListener(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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

	protected final FhirWebserviceClientProvider getFhirWebserviceClientProvider()
	{
		return clientProvider;
	}

	protected final TaskHelper getTaskHelper()
	{
		return taskHelper;
	}

	protected final ReadAccessHelper getReadAccessHelper()
	{
		return readAccessHelper;
	}

	protected final DelegateExecution getExecution()
	{
		return execution;
	}

	protected final void setExecution(DelegateExecution execution)
	{
		if (execution != null)
			this.execution = execution;
	}

	/**
	 * @return the active task from execution variables, i.e. the leading task if the main process is running or the
	 *         current task if a subprocess is running.
	 *
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getTask()
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		return execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId())
				? getLeadingTaskFromExecutionVariables()
				: getCurrentTaskFromExecutionVariables();
	}

	/**
	 * @return the current task from execution variables, the task resource that started the current process or
	 *         subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getCurrentTaskFromExecutionVariables()
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		return (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK);
	}

	/**
	 * @return the leading task from execution variables, same as current task if not in a subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final Task getLeadingTaskFromExecutionVariables()
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Task leadingTask = (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK);
		return leadingTask != null ? leadingTask : getCurrentTaskFromExecutionVariables();
	}

	/**
	 * <i>Use this method to update the process engine variable {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK},
	 * after modifying the {@link Task}.</i>
	 *
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final void updateCurrentTaskInExecutionVariables(Task task)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Objects.requireNonNull(task, "task");
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TASK, FhirResourceValues.create(task));
	}

	/**
	 * <i>Use this method to update the process engine variable
	 * {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK}, after modifying the {@link Task}.</i>
	 * <p>
	 * Updates the current task if no leading task is set.
	 *
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final void updateLeadingTaskInExecutionVariables(Task task)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Objects.requireNonNull(task, "task");
		Task leadingTask = (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK);

		if (leadingTask != null)
			execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, FhirResourceValues.create(task));
		else
			updateCurrentTaskInExecutionVariables(task);
	}
}
