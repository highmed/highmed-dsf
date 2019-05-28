package org.highmed.dsf.fhir.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.variables.DomainResourceValues;
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
		task = webserviceClient.update(task);

		// http://highmed.org/bpe/Process/processDefinitionKey
		// http://highmed.org/bpe/Process/processDefinitionKey/versionTag
		List<String> pathSegments = getPathSegments(task.getInstantiatesUri());
		String processDefinitionKey = pathSegments.get(2);
		String versionTag = pathSegments.size() == 4 ? pathSegments.get(3) : null;

		String messageName = getString(task.getInput(), Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME).orElse(null);
		String businessKey = getString(task.getInput(), Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getString(task.getInput(), Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		Map<String, Object> variables = Map.of(Constants.VARIABLE_TASK, DomainResourceValues.create(task));

		try
		{
			onMessage(businessKey, correlationKey, processDefinitionKey, versionTag, messageName, variables);
			task.setStatus(TaskStatus.COMPLETED);
		}
		catch (Exception e)
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

	private ProcessDefinition getProcessDefinition(String processDefinitionKey, String versionTag)
	{
		if (versionTag != null && !versionTag.isBlank())
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.versionTag(versionTag).latestVersion().singleResult();
		else
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.latestVersion().singleResult();
	}

	/**
	 * @param businessKey
	 *            not <code>null</code>
	 * @param correlationKey
	 *            may be <code>null</code>
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @param versionTag
	 *            not <code>null</code>
	 * @param messageName
	 *            not <code>null</code>
	 * @param variables
	 *            may be <code>null</code>
	 */
	protected void onMessage(String businessKey, String correlationKey, String processDefinitionKey, String versionTag,
			String messageName, Map<String, Object> variables)
	{
		Objects.requireNonNull(businessKey, "businessKey");
		// correlationKey may be null
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(versionTag, "versionTag");
		Objects.requireNonNull(messageName, "messageName");
		if (variables == null)
			variables = Collections.emptyMap();

		ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, versionTag);
		List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
				.processDefinitionId(processDefinition.getId()).processInstanceBusinessKey(businessKey).list();

		if (instances.size() > 1)
			logger.warn("instance-ids {}",
					instances.stream().map(ProcessInstance::getId).collect(Collectors.joining(", ", "[", "]")));

		long instanceCount = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId())
				.processInstanceBusinessKey(businessKey).count();

		if (instanceCount <= 0)
		{
			runtimeService.createMessageCorrelation(messageName).processDefinitionId(processDefinition.getId())
					.processInstanceBusinessKey(businessKey).setVariables(variables).correlateStartMessage();
		}
		else
		{
			MessageCorrelationBuilder correlation = runtimeService.createMessageCorrelation(messageName)
					.setVariables(variables).processInstanceBusinessKey(businessKey);

			if (correlationKey != null)
				correlation = correlation
						.localVariablesEqual(Map.of("correlationKey", Variables.stringValue(correlationKey)));

			correlation.correlate();
		}
	}
}
