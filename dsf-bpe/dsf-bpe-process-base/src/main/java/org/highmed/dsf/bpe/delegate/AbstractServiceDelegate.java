package org.highmed.dsf.bpe.delegate;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.AbstractDelegateAndListener;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDelegate extends AbstractDelegateAndListener
		implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	public AbstractServiceDelegate(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	public final void execute(DelegateExecution execution) throws Exception
	{
		setExecution(execution);

		try
		{
			logger.trace("Execution of task with id='{}'", execution.getCurrentActivityId());

			doExecute(execution);
		}
		// Error boundary event, do not stop process execution
		catch (BpmnError error)
		{
			Task task = getTask();

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
			Task task = getTask();

			logger.debug("Error while executing service delegate " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task with id {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					errorMessage));
			task.setStatus(Task.TaskStatus.FAILED);

			getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);

			// TODO evaluate throwing exception as alternative to stopping the process instance
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
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
}
