package org.highmed.dsf.bpe.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractServiceDelegate implements JavaDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceDelegate.class);

	private WebserviceClient webserviceClient;

	public AbstractServiceDelegate(WebserviceClient webserviceClient) {
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		try
		{
			executeService(execution);
		}
		catch (Exception e)
		{
			Task task;
			if(execution.getParentId() == null) {
				task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);
			} else
			{
				task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
			}

			Task.TaskOutputComponent failedReason =  new Task.TaskOutputComponent(
					new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, null)), new StringType(
					"process " + execution.getProcessDefinitionId() + " failed in step " + execution.getActivityInstanceId() + ", reason: " + e.getMessage()
			));

			task.setOutput(List.of(failedReason));
			task.setStatus(Task.TaskStatus.FAILED);

			webserviceClient.update(task);

			logger.error("process {} failed in step {} for task with id {}, reason: {}", execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(), e.getMessage());
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(), e.getMessage());
		}
	}

	protected abstract void executeService(DelegateExecution execution) throws Exception;
}
