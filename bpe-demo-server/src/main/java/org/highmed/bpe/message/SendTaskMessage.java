package org.highmed.bpe.message;

import java.util.Date;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SendTaskMessage implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SendTaskMessage.class);

	private final WebserviceClient fhirWebserviceClient;

	public SendTaskMessage(WebserviceClient fhirWebserviceClient)
	{
		this.fhirWebserviceClient = fhirWebserviceClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirWebserviceClient, "fhirWebserviceClient");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.info("Process instance ID {}, business key {}", execution.getProcessInstanceId(),
				execution.getBusinessKey());
		logger.info("Variables: {}", execution.getVariables());
		logger.info("Send Task Message ... " + System.identityHashCode(this));

		String messageName = (String) execution.getVariable("messageName");
		ParameterComponent messageNameInput = new ParameterComponent(
				new CodeableConcept(new Coding("http://highmed.org/fhir/CodeSystem/task-input", "message-name", null)),
				new StringType(messageName));

		String businessKey = execution.getBusinessKey();
		ParameterComponent businessKeyInput = new ParameterComponent(
				new CodeableConcept(new Coding("http://highmed.org/fhir/CodeSystem/task-input", "business-key", null)),
				new StringType(businessKey));

		String processDefinitionKey = (String) execution.getVariable("processDefinitionKey");
		String versionTag = (String) execution.getVariable("versionTag");
		String instantiatesUri = "http://highmed.org/bpe/Process/" + processDefinitionKey
				+ ((versionTag != null && !versionTag.isBlank()) ? ("/_history/" + versionTag) : "");

		Task task = new Task();
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getInput().add(messageNameInput);
		task.getInput().add(businessKeyInput);
		task.setInstantiatesUri(instantiatesUri);

		fhirWebserviceClient.create(task);
	}
}
