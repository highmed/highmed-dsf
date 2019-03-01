package org.highmed.bpe.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TaskHandler implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(TaskHandler.class);

	private final RuntimeService runtimeService;
	private final RepositoryService repositoryService;
	private final WebserviceClient webserviceClient;

	public TaskHandler(RuntimeService runtimeService, RepositoryService repositoryService,
			WebserviceClient webserviceClient)
	{
		this.runtimeService = runtimeService;
		this.repositoryService = repositoryService;
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(runtimeService, "runtimeService");
		Objects.requireNonNull(repositoryService, "repositoryService");
		Objects.requireNonNull(webserviceClient, "webserviceClient");
	}

	public void onTask(Task task)
	{
		task.setStatus(TaskStatus.INPROGRESS);
		webserviceClient.update(task);

		String processDefinitionKey = getProcessDefinitionKey(task.getInstantiatesUri());
		String versionTag = getVersionTag(task.getInstantiatesUri());
		String messageName = getString(task.getInput(), "http://highmed.org/fhir/CodeSystem/task-input",
				"message-name");
		String businessKey = getString(task.getInput(), "http://highmed.org/fhir/CodeSystem/task-input",
				"business-key");

		Map<String, Object> processVariables = task.getInput().stream()
				.collect(Collectors.toMap(this::toKey, ParameterComponent::getValue));

		try
		{
			onMessageReceived(processDefinitionKey, versionTag, messageName, businessKey, processVariables);
			task.setStatus(TaskStatus.COMPLETED);
		}
		catch (ProcessEngineException e)
		{
			logger.error("Error while handling task", e);
			task.setStatus(TaskStatus.FAILED);
		}
		finally
		{
			webserviceClient.update(task);
		}
	}

	private String getProcessDefinitionKey(String instantiatesUri)
	{
		// http://highmed.org/bpmn/Process/processDefinitionKey/_history/versionTag
		// TODO Auto-generated method stub
		return null;
	}

	private String getVersionTag(String instantiatesUri)
	{
		// http://highmed.org/bpmn/Process/processDefinitionKey/_history/versionTag
		// TODO Auto-generated method stub
		return null;
	}

	private String getString(List<ParameterComponent> list, String system, String code)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private String toKey(ParameterComponent parameterComponent)
	{
		return (parameterComponent.getType().getCodingFirstRep().getSystem() == null ? ""
				: parameterComponent.getType().getCodingFirstRep().getSystem()) + "|"
				+ (parameterComponent.getType().getCodingFirstRep().getCode() == null ? ""
						: parameterComponent.getType().getCodingFirstRep().getCode());
	}

	private void onMessageReceived(String processDefinitionKey, String versionTag, String messageName,
			String businessKey, Map<String, Object> processVariables) throws ProcessEngineException
	{
		ProcessDefinition processDefinition = getProcessDefinition(repositoryService, processDefinitionKey, versionTag);
		ProcessInstance instance = runtimeService.createProcessInstanceQuery()
				.processDefinitionId(processDefinition.getId()).processInstanceBusinessKey(businessKey).singleResult();

		if (instance != null)
		{
			List<EventSubscription> subscriptions = runtimeService.createEventSubscriptionQuery()
					.processInstanceId(instance.getId()).eventType("message").eventName(messageName).list();

			for (EventSubscription subscription : subscriptions)
			{
				runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId(),
						processVariables);
			}
		}
		else
		{
			runtimeService.startProcessInstanceById(processDefinition.getId(), businessKey, processVariables);
		}
	}

	private ProcessDefinition getProcessDefinition(RepositoryService repositoryService, String processDefinitionKey,
			String versionTag)
	{
		if (versionTag != null && !versionTag.isBlank())
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.versionTag(versionTag).latestVersion().singleResult();
		else
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.latestVersion().singleResult();
	}
}
