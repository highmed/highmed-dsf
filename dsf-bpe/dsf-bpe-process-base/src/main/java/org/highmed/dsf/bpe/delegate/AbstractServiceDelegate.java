package org.highmed.dsf.bpe.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServiceDelegate implements JavaDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private WebserviceClient webserviceClient;
	private TaskHelper taskHelper;

	public AbstractServiceDelegate(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		this.webserviceClient = webserviceClient;
		this.taskHelper = taskHelper;
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		try
		{
			doExecute(execution);
		}
		catch (Exception exception)
		{
			Task task;
			if (execution.getParentId() == null)
			{
				task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);
			}
			else
			{
				task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			}

			task = taskHelper.setErrorOutput(task, exception.getMessage(), this.getClass().getName());
			webserviceClient.update(task);

			logger.error("Process {} failed in step {} for task with id {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					exception.getMessage());
			execution.getProcessEngine().getRuntimeService()
					.deleteProcessInstance(execution.getProcessInstanceId(), exception.getMessage());
		}
	}

	/**
	 * Method called by a BPMN service task
	 *
	 * @param execution holding the process instance information and variables
	 * @throws Exception reason why process instance has failed, exception message will be stored in process associated
	 *                   fhir task resource as output
	 */
	protected abstract void doExecute(DelegateExecution execution) throws Exception;
}
