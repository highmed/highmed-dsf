package org.highmed.dsf.fhir.task;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_INSTANTIATES_URI;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_PROFILE;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;

import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
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
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper);

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
		String instantiatesUri = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_INSTANTIATES_URI);
		String messageName = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_MESSAGE_NAME);
		String profile = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_PROFILE);
		String businessKey = execution.getBusinessKey();

		// TODO see Bug https://app.camunda.com/jira/browse/CAM-9444
		// String targetOrganizationId = (String) execution.getVariable(Constants.VARIABLE_TARGET_ORGANIZATION_ID);
		// String correlationKey = (String) execution.getVariable(Constants.VARIABLE_CORRELATION_KEY);

		Target target = getTarget(execution);

		try
		{
			sendTask(target, instantiatesUri, messageName, businessKey, profile,
					getAdditionalInputParameters(execution));
		}
		catch (Exception e)
		{
			String errorMessage = "Error while sending Task (process: " + instantiatesUri + ", message-name: "
					+ messageName + ", business-key: " + businessKey + ", correlation-key: "
					+ target.getCorrelationKey() + ") to organization with identifier "
					+ target.getTargetOrganizationIdentifierValue() + ": " + e.getMessage();
			logger.warn(errorMessage);
			logger.debug("Error while sending Task", e);

			Task task = getLeadingTaskFromExecutionVariables();
			if (task != null)
				task.addOutput(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, errorMessage));
			else
				logger.warn("Leading Task null, unable to add error output");

			Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
			if (targets != null)
			{
				logger.debug("Removing target organization {} with error {} from target list",
						target.getTargetOrganizationIdentifierValue(), e.getMessage());
				targets.removeTarget(target);
			}
			else
			{
				logger.debug(
						"Target list (variable {} with type {}) not defined, ignoring target organization {} with error {}",
						BPMN_EXECUTION_VARIABLE_TARGETS, Targets.class.getCanonicalName(),
						target.getTargetOrganizationIdentifierValue(), e.getMessage());
			}
		}
	}

	/**
	 * Override this method to set a different target then the one defined in the process variable
	 * {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TARGET}
	 *
	 * @param execution
	 *            the delegate execution of this process instance
	 * @return {@link Target} that should receive the message
	 */
	protected Target getTarget(DelegateExecution execution)
	{
		return (Target) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGET);
	}

	/**
	 * Override this method to add additional input parameters to the task resource being send
	 *
	 * @param execution
	 *            the delegate execution of this process instance
	 * @return {@link Stream} of {@link ParameterComponent}s to be added as input parameters
	 */
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.empty();
	}

	protected void sendTask(Target target, String instantiatesUri, String messageName, String businessKey,
			String profile, Stream<ParameterComponent> additionalInputParameters)
	{
		if (messageName.isEmpty() || instantiatesUri.isEmpty())
			throw new IllegalStateException("Next process-id or message-name not definied");

		Task task = new Task();
		task.setMeta(new Meta().addProfile(profile));
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.setRequester(getRequester());
		task.getRestriction().addRecipient(getRecipient(target));
		task.setInstantiatesUri(instantiatesUri);

		ParameterComponent messageNameInput = new ParameterComponent(
				new CodeableConcept(
						new Coding(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME, null)),
				new StringType(messageName));
		task.getInput().add(messageNameInput);

		ParameterComponent businessKeyInput = new ParameterComponent(
				new CodeableConcept(
						new Coding(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY, null)),
				new StringType(businessKey));
		task.getInput().add(businessKeyInput);

		String correlationKey = target.getCorrelationKey();
		if (correlationKey != null)
		{
			ParameterComponent correlationKeyInput = new ParameterComponent(
					new CodeableConcept(
							new Coding(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY, null)),
					new StringType(correlationKey));
			task.getInput().add(correlationKeyInput);
		}

		additionalInputParameters.forEach(task.getInput()::add);

		FhirWebserviceClient client = getFhirWebserviceClientProvider()
				.getWebserviceClient(target.getTargetEndpointUrl());

		logger.info("Sending task {} to {} [message: {}, businessKey: {}, correlationKey: {}, endpoint: {}]",
				task.getInstantiatesUri(), target.getTargetOrganizationIdentifierValue(), messageName, businessKey,
				correlationKey, client.getBaseUrl());
		logger.trace("Task resource to send: {}", fhirContext.newJsonParser().encodeResourceToString(task));

		client.withMinimalReturn().create(task);
	}

	protected Reference getRecipient(Target target)
	{
		return new Reference().setType("Organization")
				.setIdentifier(new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
						.setValue(target.getTargetOrganizationIdentifierValue()));
	}

	protected Reference getRequester()
	{
		return new Reference().setType("Organization")
				.setIdentifier(new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
						.setValue(getOrganizationProvider().getLocalIdentifierValue()));
	}

	protected final OrganizationProvider getOrganizationProvider()
	{
		return organizationProvider;
	}
}
