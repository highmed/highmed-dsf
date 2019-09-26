package org.highmed.dsf.bpe.delegate;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDelegate implements JavaDelegate, InitializingBean
{

	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private FhirWebserviceClient webserviceClient;
	private TaskHelper taskHelper;

	public AbstractServiceDelegate(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		this.webserviceClient = webserviceClient;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(webserviceClient, "webserviceClient");
		Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		try
		{
			doExecute(execution);
			doExecutePlugin(execution);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();

			Task task;
			if (execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId()))
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

	private void doExecutePlugin(DelegateExecution execution)
	{
		// TODO: implement plugin system for individual checks in different medics, like:
		//       - PI check
		//       - Cohort characteristics check
		//       - Queries check
		//       - Requester check
		//       - ...
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
