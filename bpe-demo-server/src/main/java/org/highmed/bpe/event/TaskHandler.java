package org.highmed.bpe.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.UriComponentsBuilder;

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

		// http://highmed.org/bpe/Process/processDefinitionKey
		// http://highmed.org/bpe/Process/processDefinitionKey/_history/versionTag
		List<String> pathSegments = getPathSegments(task.getInstantiatesUri());
		String processDefinitionKey = pathSegments.get(2);
		String versionTag = pathSegments.size() == 5 ? pathSegments.get(4) : null;
		String messageName = getString(task.getInput(), "http://highmed.org/fhir/CodeSystem/task-input", "message-name")
				.orElse(null);
		String businessKey = getString(task.getInput(), "http://highmed.org/fhir/CodeSystem/task-input", "business-key")
				.orElse(null);

		Map<String, Object> processVariables = task.getInput().stream()
				.collect(Collectors.toMap(this::toKey, ParameterComponent::getValue));

		try
		{
			onMessageReceivedContinueOrStartProcessInstance(processDefinitionKey, versionTag, messageName, businessKey,
					processVariables);
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

	private List<String> getPathSegments(String istantiatesUri)
	{
		return UriComponentsBuilder.fromUriString(istantiatesUri).build().getPathSegments();
	}

	private Optional<String> getString(List<ParameterComponent> list, String system, String code)
	{
		return list.stream().filter(c -> c.getValue() instanceof StringType)
				.filter(c -> c.getType().getCoding().stream()
						.anyMatch(co -> system.equals(co.getSystem()) && code.equals(co.getCode())))
				.map(c -> ((StringType) c.getValue()).asStringValue()).findFirst();
	}

	private String toKey(ParameterComponent parameterComponent)
	{
		return (parameterComponent.getType().getCodingFirstRep().getSystem() == null ? ""
				: parameterComponent.getType().getCodingFirstRep().getSystem()) + "|"
				+ (parameterComponent.getType().getCodingFirstRep().getCode() == null ? ""
						: parameterComponent.getType().getCodingFirstRep().getCode());
	}

	private ProcessInstance onMessageReceivedContinueOrStartProcessInstance(String processDefinitionKey,
			String versionTag, String messageName, String businessKey, Map<String, Object> processVariables)
			throws ProcessEngineException
	{
		ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, versionTag);

		if (processDefinition == null)
			throw new ProcessEngineException("ProcessDefinition with key " + processDefinitionKey
					+ (versionTag != null && !versionTag.isBlank() ? (" and versionTag " + versionTag) : "")
					+ " not found");

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

			return instance;
		}
		else
		{
			return runtimeService.startProcessInstanceById(processDefinition.getId(), businessKey, processVariables);
		}
	}

	private ProcessDefinition getProcessDefinition(String processDefinitionKey, String versionTag)
	{
		if (versionTag != null && !versionTag.isBlank())
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.versionTag(versionTag).latestVersion().singleResult();
		else
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.latestVersion().singleResult();
	}
}
