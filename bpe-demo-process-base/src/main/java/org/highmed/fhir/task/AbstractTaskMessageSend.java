package org.highmed.fhir.task;

import java.util.Date;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.fhir.client.ClientProvider;
import org.highmed.fhir.client.WebserviceClient;
import org.highmed.fhir.organization.OrganizationProvider;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class AbstractTaskMessageSend implements JavaDelegate, InitializingBean
{
	public static final String VARIABLE_MESSAGE_NAME = "messageName";
	public static final String VARIABLE_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	public static final String VARIABLE_VERSION_TAG = "versionTag";
	public static final String VARIABLE_TARGET_ORGANIZATION = "targetOrganization";
	public static final String VARIABLE_CORRELATION_KEY = "correlationKey";

	private static final Logger logger = LoggerFactory.getLogger(AbstractTaskMessageSend.class);

	private final ClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;

	public AbstractTaskMessageSend(OrganizationProvider organizationProvider, ClientProvider clientProvider)
	{
		this.organizationProvider = organizationProvider;
		this.clientProvider = clientProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		Organization target = (Organization) execution.getVariable(VARIABLE_TARGET_ORGANIZATION);
		String processDefinitionKey = (String) execution.getVariable(VARIABLE_PROCESS_DEFINITION_KEY);
		String versionTag = (String) execution.getVariable(VARIABLE_VERSION_TAG);
		String messageName = (String) execution.getVariable(VARIABLE_MESSAGE_NAME);
		String businessKey = execution.getBusinessKey();
		String correlationKey = (String) execution.getVariable(VARIABLE_CORRELATION_KEY);

		sendTask(target, processDefinitionKey, versionTag, messageName, businessKey, correlationKey);
	}

	protected void sendTask(Organization target, String processDefinitionKey, String versionTag, String messageName,
			String businessKey, String correlationKey, ParameterComponent... additionalInputParameters)
	{
		if (messageName.isEmpty() || processDefinitionKey.isEmpty())
			throw new IllegalStateException("Next process-id or message-name not definied");

		Task task = new Task();
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.setRequester(new Reference(organizationProvider.getLocalOrganization().getIdElement()));
		task.getRestriction().addRecipient(new Reference(target.getIdElement()));

		// http://highmed.org/bpe/Process/processDefinitionKey
		// http://highmed.org/bpe/Process/processDefinitionKey/versionTag
		String instantiatesUri = "http://highmed.org/bpe/Process/" + processDefinitionKey
				+ (versionTag != null && !versionTag.isEmpty() ? ("/" + versionTag) : "");
		task.setInstantiatesUri(instantiatesUri);

		ParameterComponent messageNameInput = new ParameterComponent(
				new CodeableConcept(
						new Coding("http://highmed.org/fhir/CodeSystem/bpmn-message", "message-name", null)),
				new StringType(messageName));
		task.getInput().add(messageNameInput);

		ParameterComponent businessKeyInput = new ParameterComponent(
				new CodeableConcept(
						new Coding("http://highmed.org/fhir/CodeSystem/bpmn-message", "business-key", null)),
				new StringType(businessKey));
		task.getInput().add(businessKeyInput);

		if (correlationKey != null)
		{
			ParameterComponent correlationKeyInput = new ParameterComponent(
					new CodeableConcept(
							new Coding("http://highmed.org/fhir/CodeSystem/bpmn-message", "correlation-key", null)),
					new StringType(correlationKey));
			task.getInput().add(correlationKeyInput);
		}

		for (ParameterComponent param : additionalInputParameters)
			task.getInput().add(param);

		WebserviceClient client = clientProvider.getRemoteClient(target.getIdElement());

		logger.info("Sending task {} to organization {} ({})", task.getInstantiatesUri(), target.getName(),
				target.getIdElement().getIdPart());
		client.create(task);
	}

	/*
	 * private Optional<String> getMessageName(DelegateExecution execution) { String variable = (String)
	 * execution.getVariable(VARIABLE_MESSAGE_NAME); if (variable != null) return Optional.of(variable);
	 * 
	 * ThrowEvent event = (ThrowEvent) execution.getBpmnModelElementInstance(); for (EventDefinition eventDefinition :
	 * event.getEventDefinitions()) { if (eventDefinition instanceof MessageEventDefinition) { MessageEventDefinition
	 * med = (MessageEventDefinition) eventDefinition; Message message = med.getMessage(); return
	 * Optional.of(message.getName()); } }
	 * 
	 * return Optional.empty(); }
	 */

	/*
	 * private Optional<String> getProcessDefinitionKey(DelegateExecution execution) { String variable = (String)
	 * execution.getVariable(VARIABLE_PROCESS_DEFINITION_KEY); if (variable != null) return Optional.of(variable);
	 * 
	 * final String currentElementId = execution.getBpmnModelElementInstance().getId(); for (Collaboration collaboration
	 * : execution.getBpmnModelInstance().getModelElementsByType(Collaboration.class)) { for (MessageFlow messageFlow :
	 * collaboration.getMessageFlows()) { if (messageFlow.getSource().getId().contentEquals(currentElementId)) {
	 * InteractionNode target = messageFlow.getTarget(); if (target instanceof CatchEvent) { CatchEvent catchEvent =
	 * (CatchEvent) target; BpmnModelElementInstance scope = catchEvent.getScope(); if (scope instanceof
	 * org.camunda.bpm.model.bpmn.instance.Process) return Optional.of(((org.camunda.bpm.model.bpmn.instance.Process)
	 * scope).getId()); else if (scope instanceof SubProcess) { while (scope instanceof SubProcess) scope =
	 * scope.getScope();
	 * 
	 * if (scope instanceof org.camunda.bpm.model.bpmn.instance.Process) return
	 * Optional.of(((org.camunda.bpm.model.bpmn.instance.Process) scope).getId()); } } } } }
	 * 
	 * return Optional.empty(); }
	 */

	/*
	 * private Optional<String> getVersionTag(DelegateExecution execution, Optional<String> processDefinitionKey) {
	 * String variable = (String) execution.getVariable(VARIABLE_VERSION_TAG); if (variable != null) return
	 * Optional.of(variable);
	 * 
	 * if (processDefinitionKey.isEmpty()) return Optional.empty();
	 * 
	 * ModelElementInstance element = execution.getBpmnModelInstance().getModelElementById(processDefinitionKey.get());
	 * if (element instanceof org.camunda.bpm.model.bpmn.instance.Process) { String version =
	 * ((org.camunda.bpm.model.bpmn.instance.Process) element).getCamundaVersionTag(); return
	 * Optional.ofNullable(version == null || version.isBlank() ? null : version); } else return Optional.empty(); }
	 */
}
