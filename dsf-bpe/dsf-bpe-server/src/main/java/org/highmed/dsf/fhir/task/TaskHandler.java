package org.highmed.dsf.fhir.task;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TaskHandler implements ResourceHandler<Task>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(TaskHandler.class);

	private static final String INSTANTIATES_URI_PATTERN_STRING = "(?<processUrl>http://(?<processDomain>(?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))/bpe/Process/(?<processDefinitionKey>[-\\w]+))/(?<processVersion>\\d+\\.\\d+\\.\\d+)";
	private static final Pattern INSTANTIATES_URI_PATTERN = Pattern.compile(INSTANTIATES_URI_PATTERN_STRING);

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

	public void onResource(Task task)
	{
		task.setStatus(Task.TaskStatus.INPROGRESS);
		task = webserviceClient.update(task);

		Matcher matcher = INSTANTIATES_URI_PATTERN.matcher(task.getInstantiatesUri());
		if (!matcher.matches())
			throw new IllegalStateException("InstantiatesUri of Task with id " + task.getIdElement().getIdPart()
					+ " does not match " + INSTANTIATES_URI_PATTERN_STRING);

		String processDomain = matcher.group("processDomain").replace(".", "");
		String processDefinitionKey = matcher.group("processDefinitionKey");
		String processVersion = matcher.group("processVersion");

		String messageName = taskHelper.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME).orElse(null);
		String businessKey = taskHelper.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = taskHelper.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		Map<String, Object> variables = Map.of(BPMN_EXECUTION_VARIABLE_TASK, FhirResourceValues.create(task));

		try
		{
			onMessage(businessKey, correlationKey, processDomain, processDefinitionKey, processVersion, messageName,
					variables);
		}
		catch (Exception exception)
		{
			logger.error("Error while handling Task", exception);

			Task.TaskOutputComponent errorOutput = taskHelper.createOutput(CODESYSTEM_HIGHMED_BPMN,
					CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					exception.getClass().getName() + ": " + exception.getMessage());
			task.addOutput(errorOutput);
			task.setStatus(Task.TaskStatus.FAILED);
			webserviceClient.update(task);
		}
	}

	/**
	 * @param businessKey
	 *            may be <code>null</code>
	 * @param correlationKey
	 *            may be <code>null</code>
	 * @param processDomain
	 *            not <code>null</code>
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @param processVersion
	 *            not <code>null</code>
	 * @param messageName
	 *            not <code>null</code>
	 * @param variables
	 *            may be <code>null</code>
	 */
	protected void onMessage(String businessKey, String correlationKey, String processDomain,
			String processDefinitionKey, String processVersion, String messageName, Map<String, Object> variables)
	{
		// businessKey may be null
		// correlationKey may be null
		Objects.requireNonNull(processDomain, "processDomain");
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(processVersion, "processVersion");
		Objects.requireNonNull(messageName, "messageName");

		if (variables == null)
			variables = Collections.emptyMap();

		ProcessDefinition processDefinition = getProcessDefinition(processDomain, processDefinitionKey, processVersion);

		if (processDefinition == null)
		{
			if (processVersion != null && !processVersion.isBlank())
			{
				logger.warn(
						"Process with id: {}_{} and version: {} not found, this is likely due to a mismatch between ActivityDefinition.url and Process.id (process definition key)",
						processDomain, processDefinitionKey, processVersion);
				throw new RuntimeException("Process with id: " + processDomain + "_" + processDefinitionKey
						+ " and version: " + processVersion + " not found");
			}
			else
			{
				logger.warn(
						"Process with id: {}_{} not found, this is likely due to a mismatch between ActivityDefinition.url and Process.id (process definition key)",
						processDomain, processDefinitionKey);
				throw new RuntimeException(
						"Process with id: " + processDomain + "_" + processDefinitionKey + " not found");
			}
		}

		if (businessKey == null)
		{
			runtimeService.startProcessInstanceByMessageAndProcessDefinitionId(messageName, processDefinition.getId(),
					UUID.randomUUID().toString(), variables);
		}
		else
		{
			List<ProcessInstance> instances = getProcessInstanceQuery(processDefinition, businessKey).list();
			List<ProcessInstance> instancesWithAlternativeBusinessKey = getAlternativeProcessInstanceQuery(
					processDefinition, businessKey).list();

			if (instances.size() + instancesWithAlternativeBusinessKey.size() > 1)
				logger.warn("instance-ids {}",
						Stream.concat(instances.stream(), instancesWithAlternativeBusinessKey.stream())
								.map(ProcessInstance::getId).collect(Collectors.joining(", ", "[", "]")));

			if (instances.size() + instancesWithAlternativeBusinessKey.size() <= 0)
			{
				runtimeService.createMessageCorrelation(messageName).processDefinitionId(processDefinition.getId())
						.processInstanceBusinessKey(businessKey).setVariables(variables).correlateStartMessage();
			}
			else
			{
				MessageCorrelationBuilder correlation;

				if (instances.size() > 0)
					correlation = runtimeService.createMessageCorrelation(messageName).setVariables(variables)
							.processInstanceBusinessKey(businessKey);
				else
					correlation = runtimeService.createMessageCorrelation(messageName).setVariables(variables)
							.processInstanceVariableEquals(BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY,
									businessKey);

				if (correlationKey != null)
					correlation = correlation.localVariableEquals("correlationKey", correlationKey);

				// throws MismatchingMessageCorrelationException - if none or more than one execution or process
				// definition is matched by the correlation
				correlation.correlate();
			}
		}
	}

	private ProcessDefinition getProcessDefinition(String processDomain, String processDefinitionKey,
			String processVersion)
	{
		if (processVersion != null && !processVersion.isBlank())
			return repositoryService.createProcessDefinitionQuery().active()
					.processDefinitionKey(processDomain + "_" + processDefinitionKey).versionTag(processVersion).list()
					.stream().sorted(Comparator.comparing(ProcessDefinition::getVersion).reversed()).findFirst()
					.orElse(null);
		else
			return repositoryService.createProcessDefinitionQuery().active()
					.processDefinitionKey(processDomain + "_" + processDefinitionKey).latestVersion().singleResult();
	}

	private ProcessInstanceQuery getProcessInstanceQuery(ProcessDefinition processDefinition, String businessKey)
	{
		return runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId())
				.processInstanceBusinessKey(businessKey);
	}

	private ProcessInstanceQuery getAlternativeProcessInstanceQuery(ProcessDefinition processDefinition,
			String businessKey)
	{
		return runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId())
				.variableValueEquals(BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY, businessKey);
	}
}
