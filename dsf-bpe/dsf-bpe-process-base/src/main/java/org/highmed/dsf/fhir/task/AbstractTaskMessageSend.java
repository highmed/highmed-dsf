package org.highmed.dsf.fhir.task;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY;
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
import java.util.UUID;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
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
			sendTask(execution, target, instantiatesUri, messageName, businessKey, profile,
					getAdditionalInputParameters(execution));
		}
		catch (Exception e)
		{
			String errorMessage = "Error while sending Task (process: " + instantiatesUri + ", message-name: "
					+ messageName + ", business-key: " + businessKey + ", correlation-key: "
					+ target.getCorrelationKey() + ") to organization with identifier "
					+ target.getOrganizationIdentifierValue() + ", endpoint with identifier "
					+ target.getEndpointIdentifierValue() + ": " + e.getMessage();
			logger.warn(errorMessage);
			logger.debug("Error while sending Task", e);

			if (execution.getBpmnModelElementInstance() instanceof IntermediateThrowEvent)
				handleIntermediateThrowEventError(execution, e, errorMessage);
			else if (execution.getBpmnModelElementInstance() instanceof EndEvent)
				handleEndEventError(execution, e, errorMessage);
			else if (execution.getBpmnModelElementInstance() instanceof SendTask)
				handleSendTaskError(execution, e, errorMessage);
			else
				logger.warn("Error handling for {} not implemented",
						execution.getBpmnModelElementInstance().getClass().getName());
		}
	}

	protected void addErrorMessage(Task task, String errorMessage)
	{
		task.addOutput(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
				errorMessage));
	}

	protected void handleIntermediateThrowEventError(DelegateExecution execution, Exception exception,
			String errorMessage)
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);

		logger.debug("Error while executing Task message send " + getClass().getName(), exception);
		logger.error("Process {} has fatal error in step {} for task {}, reason: {}",
				execution.getProcessDefinitionId(), execution.getActivityInstanceId(), getTaskAbsoluteUrl(task),
				exception.getMessage());

		try
		{
			if (task != null)
			{
				addErrorMessage(task, errorMessage);
				task.setStatus(Task.TaskStatus.FAILED);
				getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);
			}
			else
				logger.warn("Leading Task null, unable update Task with failed state");
		}
		finally
		{
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
	}

	protected void handleEndEventError(DelegateExecution execution, Exception exception, String errorMessage)
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);

		logger.debug("Error while executing Task message send " + getClass().getName(), exception);
		logger.error("Process {} has fatal error in step {} for task {}, reason: {}",
				execution.getProcessDefinitionId(), execution.getActivityInstanceId(), getTaskAbsoluteUrl(task),
				exception.getMessage());

		if (task != null)
		{
			addErrorMessage(task, errorMessage);
			task.setStatus(Task.TaskStatus.FAILED);
			updateLeadingTaskInExecutionVariables(execution, task);
		}
		else
			logger.warn("Leading Task null, unable to set failed state");

		// Task update and process-end handled by EndListener
	}

	protected void handleSendTaskError(DelegateExecution execution, Exception exception, String errorMessage)
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);
		Targets targets = getTargets(execution);

		// if we are a multi instance message send task, remove target
		if (targets != null)
		{
			addErrorMessage(task, errorMessage);
			updateLeadingTaskInExecutionVariables(execution, task);

			Target target = getTarget(execution);
			targets = targets.removeByEndpointIdentifierValue(target);
			updateTargets(execution, targets);
			logger.debug("Target organization {}, endpoint {} with error {} removed from target list",
					target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(),
					exception.getMessage());

			if (targets.isEmpty())
			{
				logger.debug("Error while executing Task message send " + getClass().getName(), exception);
				logger.error("Process {} has fatal error in step {} for task {}, last reason: {}",
						execution.getProcessDefinitionId(), execution.getActivityInstanceId(), getTaskAbsoluteUrl(task),
						exception.getMessage());

				try
				{
					if (task != null)
					{
						task.setStatus(Task.TaskStatus.FAILED);
						getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);
					}
					else
						logger.warn("Leading Task null, unable update Task with failed state");
				}
				finally
				{
					execution.getProcessEngine().getRuntimeService()
							.deleteProcessInstance(execution.getProcessInstanceId(), exception.getMessage());
				}
			}
		}
	}

	/**
	 * <i>Override this method if the {@link Target} variable is stored in a different process engine variable other
	 * then {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TARGET}.</i>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @return {@link Target} that should receive the message
	 */
	protected Target getTarget(DelegateExecution execution)
	{
		return (Target) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGET);
	}

	/**
	 * <i>Override this method if the {@link Targets} variable is stored in a different process engine variable other
	 * then {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TARGETS}.</i>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @return {@link Targets} that should receive the message
	 */
	protected Targets getTargets(DelegateExecution execution)
	{
		return (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
	}

	/**
	 * <i>Override this method if the {@link Targets} variable should stored in a different process engine variable
	 * other then {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TARGETS}.</i>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @param targets
	 *            the targets to save in process engine variable {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TARGETS}
	 */
	protected void updateTargets(DelegateExecution execution, Targets targets)
	{
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(targets));
	}

	/**
	 * <i>Override this method to add additional input parameters to the task resource being send.</i>
	 *
	 * @param execution
	 *            the delegate execution of this process instance
	 * @return {@link Stream} of {@link ParameterComponent}s to be added as input parameters
	 */
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.empty();
	}

	/**
	 * Generates an alternative business-key and stores it as a process variable with name
	 * {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY}<br>
	 * <br>
	 * <i>Use this method in combination with overriding
	 * {@link #sendTask(DelegateExecution, Target, String, String, String, String, Stream)} to use an alternative
	 * business-key with the communication target.</i>
	 *
	 * <pre>
	 * &#64;Override
	 * protected void sendTask(DelegateExecution execution, Target target, String instantiatesUri, String messageName,
	 * 		String businessKey, String profile, Stream&lt;ParameterComponent&gt; additionalInputParameters)
	 * {
	 * 	String alternativeBusinesKey = createAndSaveAlternativeBusinessKey();
	 * 	super.sendTask(execution, target, instantiatesUri, messageName, alternativeBusinesKey, profile,
	 * 			additionalInputParameters);
	 * }
	 * </pre>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @return the alternative business-key stored as variable
	 *         {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY}
	 */
	protected final String createAndSaveAlternativeBusinessKey(DelegateExecution execution)
	{
		String alternativeBusinessKey = UUID.randomUUID().toString();
		execution.setVariable(BPMN_EXECUTION_VARIABLE_ALTERNATIVE_BUSINESS_KEY, alternativeBusinessKey);

		return alternativeBusinessKey;
	}

	protected void sendTask(DelegateExecution execution, Target target, String instantiatesUri, String messageName,
			String businessKey, String profile, Stream<ParameterComponent> additionalInputParameters)
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

		FhirWebserviceClient client = getFhirWebserviceClientProvider().getWebserviceClient(target.getEndpointUrl());

		logger.info("Sending task {} to {}/{} [message: {}, businessKey: {}, correlationKey: {}, endpoint: {}]",
				task.getInstantiatesUri(), target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(),
				messageName, businessKey, correlationKey, client.getBaseUrl());
		logger.trace("Task resource to send: {}", fhirContext.newJsonParser().encodeResourceToString(task));

		doSend(client, task);
	}

	/**
	 * <i>Override this method to modify the remote task create behavior, e.g. to implement retries</i>
	 *
	 * <pre>
	 * <code>
	 * &#64;Override
	 * protected void doSend(FhirWebserviceClient client, Task task)
	 * {
	 *     client.withMinimalReturn().withRetry(2).create(task);
	 * }
	 * </code>
	 * </pre>
	 *
	 * @param client
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 */
	protected void doSend(FhirWebserviceClient client, Task task)
	{
		client.withMinimalReturn().create(task);
	}

	protected Reference getRecipient(Target target)
	{
		return new Reference().setType("Organization")
				.setIdentifier(new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
						.setValue(target.getOrganizationIdentifierValue()));
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
