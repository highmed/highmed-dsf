package org.highmed.dsf.fhir.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.variables.DomainResourceValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.UriComponentsBuilder;

public class TaskHandler implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(TaskHandler.class);

	private final RuntimeService runtimeService;
	private final RepositoryService repositoryService;
	private final FhirWebserviceClient webserviceClient;
	private final TaskHelper taskHelper;

	public TaskHandler(RuntimeService runtimeService, RepositoryService repositoryService,
			FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		this.runtimeService = runtimeService;
		this.repositoryService = repositoryService;
		this.webserviceClient = webserviceClient;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(runtimeService, "runtimeService");
		Objects.requireNonNull(repositoryService, "repositoryService");
	}

	public void onTask(Task task)
	{
		task.setStatus(Task.TaskStatus.INPROGRESS);
		task = webserviceClient.update(task);

		// http://highmed.org/bpe/Process/processDefinitionKey
		// http://highmed.org/bpe/Process/processDefinitionKey/versionTag
		List<String> pathSegments = getPathSegments(task.getInstantiatesUri());
		String processDefinitionKey = pathSegments.get(2);
		String versionTag = pathSegments.size() == 4 ? pathSegments.get(3) : null;

		String messageName = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME).orElse(null);
		String businessKey = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		Map<String, Object> variables = Map.of(Constants.VARIABLE_TASK, DomainResourceValues.create(task));

		try
		{
			onMessage(businessKey, correlationKey, processDefinitionKey, versionTag, messageName, variables);
		}
		catch (Exception exception)
		{
			Task.TaskOutputComponent errorOutput = taskHelper.createOutput(Constants.CODESYSTEM_HIGHMED_BPMN,
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, exception.getMessage());
			task.addOutput(errorOutput);
			task.setStatus(Task.TaskStatus.FAILED);
			webserviceClient.update(task);
		}
	}

	private List<String> getPathSegments(String istantiatesUri)
	{
		return UriComponentsBuilder.fromUriString(istantiatesUri).build().getPathSegments();
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
	 *            may be <code>null</code>
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
		// businessKey may be null
		// correlationKey may be null
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(versionTag, "versionTag");
		Objects.requireNonNull(messageName, "messageName");

		if (variables == null)
			variables = Collections.emptyMap();

		ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, versionTag);

		if (businessKey == null)
		{
			runtimeService.startProcessInstanceById(processDefinition.getId(), UUID.randomUUID().toString(), variables);
		}
		else
		{
			List<ProcessInstance> instances = getProcessInstanceQuery(processDefinition, businessKey).list();

			if (instances.size() > 1)
				logger.warn("instance-ids {}",
						instances.stream().map(ProcessInstance::getId).collect(Collectors.joining(", ", "[", "]")));

			long instanceCount = getProcessInstanceQuery(processDefinition, businessKey).count();

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

	private ProcessInstanceQuery getProcessInstanceQuery(ProcessDefinition processDefinition, String businessKey)
	{
		return runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId())
				.processInstanceBusinessKey(businessKey);
	}
}
