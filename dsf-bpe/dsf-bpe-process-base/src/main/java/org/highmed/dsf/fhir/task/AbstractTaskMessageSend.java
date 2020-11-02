package org.highmed.dsf.fhir.task;

import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class AbstractTaskMessageSend extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractTaskMessageSend.class);

	private final OrganizationProvider organizationProvider;
	private final FhirContext fhirContext;

	public AbstractTaskMessageSend(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
		this.fhirContext = fhirContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		String processDefinitionKey = (String) execution.getVariable(ConstantsBase.VARIABLE_PROCESS_DEFINITION_KEY);
		String versionTag = (String) execution.getVariable(ConstantsBase.VARIABLE_VERSION_TAG);
		String messageName = (String) execution.getVariable(ConstantsBase.VARIABLE_MESSAGE_NAME);
		String profile = (String) execution.getVariable(ConstantsBase.VARIABLE_PROFILE);
		String businessKey = execution.getBusinessKey();

		// TODO see Bug https://app.camunda.com/jira/browse/CAM-9444
		// String targetOrganizationId = (String) execution.getVariable(Constants.VARIABLE_TARGET_ORGANIZATION_ID);
		// String correlationKey = (String) execution.getVariable(Constants.VARIABLE_CORRELATION_KEY);

		MultiInstanceTarget target = getMultiInstanceTarget(execution);

		try
		{
			sendTask(target.getTargetOrganizationIdentifierValue(), processDefinitionKey, versionTag, messageName,
					businessKey, target.getCorrelationKey(), profile, getAdditionalInputParameters(execution));
		}
		catch (Exception e)
		{
			String errorMessage = "Error while sending Task (process: " + processDefinitionKey + ", version: "
					+ versionTag + ", message-name: " + messageName + ", business-key: " + businessKey
					+ ", correlation-key: " + target.getCorrelationKey() + ") to organization with identifier "
					+ target.getTargetOrganizationIdentifierValue() + ": " + e.getMessage();
			logger.warn(errorMessage);
			logger.debug("Error while sending Task", e);

			logger.debug("Removing target organization {} with error {} from multi instance target list",
					target.getTargetOrganizationIdentifierValue(), e.getMessage());
			MultiInstanceTargets targets = (MultiInstanceTargets) execution
					.getVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGETS);
			targets.removeTarget(target);

			Task task = getLeadingTaskFromExecutionVariables().addOutput(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
					ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));
			execution.setVariable(ConstantsBase.VARIABLE_LEADING_TASK, task);
		}
	}

	/**
	 * Override this method to set a different multiinstance target then the one defined in the process variable
	 * {@link ConstantsBase#VARIABLE_MULTI_INSTANCE_TARGET}
	 *
	 * @param execution the delegate execution of this process instance
	 * @return {@link MultiInstanceTarget} that should receive the message
	 */
	protected MultiInstanceTarget getMultiInstanceTarget(DelegateExecution execution)
	{
		return (MultiInstanceTarget) execution.getVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGET);
	}

	/**
	 * Override this method to add additional input parameters to the task resource being send
	 *
	 * @return {@link Stream} of {@link ParameterComponent}s to be added as input parameters
	 */
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.empty();
	}

	protected void sendTask(String targetOrganizationIdentifierValue, String processDefinitionKey, String versionTag,
			String messageName, String businessKey, String correlationKey, String profile,
			Stream<ParameterComponent> additionalInputParameters)
	{
		if (messageName.isEmpty() || processDefinitionKey.isEmpty())
			throw new IllegalStateException("Next process-id or message-name not definied");

		Task task = new Task();
		task.setMeta(new Meta().addProfile(profile));
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.setRequester(
				new Reference().setType("Organization").setIdentifier(organizationProvider.getLocalIdentifier()));
		Reference targetReference = new Reference();
		targetReference.setType("Organization");
		targetReference.getIdentifier().setSystem(organizationProvider.getDefaultIdentifierSystem())
				.setValue(targetOrganizationIdentifierValue);
		task.getRestriction().addRecipient(targetReference);

		// http://highmed.org/bpe/Process/processDefinitionKey
		// http://highmed.org/bpe/Process/processDefinitionKey/versionTag
		String instantiatesUri = ConstantsBase.PROCESS_URI_BASE + processDefinitionKey + (versionTag != null
				&& !versionTag.isEmpty() ? ("/" + versionTag) : "");
		task.setInstantiatesUri(instantiatesUri);

		ParameterComponent messageNameInput = new ParameterComponent(new CodeableConcept(
				new Coding(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME, null)), new StringType(messageName));
		task.getInput().add(messageNameInput);

		ParameterComponent businessKeyInput = new ParameterComponent(new CodeableConcept(
				new Coding(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY, null)), new StringType(businessKey));
		task.getInput().add(businessKeyInput);

		if (correlationKey != null)
		{
			ParameterComponent correlationKeyInput = new ParameterComponent(new CodeableConcept(
					new Coding(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
							ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY, null)),
					new StringType(correlationKey));
			task.getInput().add(correlationKeyInput);
		}

		additionalInputParameters.forEach(task.getInput()::add);

		FhirWebserviceClient client = getFhirClient(task, targetOrganizationIdentifierValue);

		logger.info("Sending task {} to {} [message: {}, businessKey: {}, correlationKey: {}, endpoint: {}]",
				task.getInstantiatesUri(), targetOrganizationIdentifierValue, messageName, businessKey, correlationKey,
				client.getBaseUrl());
		logger.trace("Task resource to send: {}", fhirContext.newJsonParser().encodeResourceToString(task));

		client.withMinimalReturn().create(task);
	}

	private FhirWebserviceClient getFhirClient(Task task, String targetOrganizationIdentifierValue)
	{
		if (task.getRequester().equalsDeep(task.getRestriction().getRecipient().get(0)))
		{
			logger.trace("Using local webservice client");
			return getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			logger.trace("Using remote webservice client");
			return getFhirWebserviceClientProvider()
					.getRemoteWebserviceClient(organizationProvider.getDefaultIdentifierSystem(),
							targetOrganizationIdentifierValue);
		}
	}

	protected final OrganizationProvider getOrganizationProvider()
	{
		return organizationProvider;
	}
}
