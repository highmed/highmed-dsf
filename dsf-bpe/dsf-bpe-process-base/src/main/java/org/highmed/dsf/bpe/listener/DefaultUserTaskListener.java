package org.highmed.dsf.bpe.listener;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_ID;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DefaultUserTaskListener implements TaskListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultUserTaskListener.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;
	private final QuestionnaireResponseHelper questionnaireResponseHelper;
	private final TaskHelper taskHelper;
	private final ReadAccessHelper readAccessHelper;

	public DefaultUserTaskListener(FhirWebserviceClientProvider clientProvider,
			OrganizationProvider organizationProvider, QuestionnaireResponseHelper questionnaireResponseHelper,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper)
	{
		this.clientProvider = clientProvider;
		this.organizationProvider = organizationProvider;
		this.questionnaireResponseHelper = questionnaireResponseHelper;
		this.taskHelper = taskHelper;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(questionnaireResponseHelper, "questionnaireResponseHelper");
		Objects.requireNonNull(taskHelper, "taskHelper");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public final void notify(DelegateTask userTask)
	{
		DelegateExecution execution = userTask.getExecution();

		try
		{
			logger.trace("Execution of user task with id='{}'", execution.getCurrentActivityId());

			String questionnaireUrlWithVersion = userTask.getBpmnModelElementInstance().getCamundaFormKey();
			Questionnaire questionnaire = readQuestionnaire(questionnaireUrlWithVersion);

			String businessKey = execution.getBusinessKey();
			String userTaskId = userTask.getId();

			QuestionnaireResponse questionnaireResponse = createDefaultQuestionnaireResponse(
					questionnaireUrlWithVersion, businessKey, userTaskId);
			transformQuestionnaireItemsToQuestionnaireResponseItems(questionnaireResponse, questionnaire);

			beforeQuestionnaireResponseCreate(userTask, questionnaireResponse);
			checkQuestionnaireResponse(questionnaireResponse);

			QuestionnaireResponse created = clientProvider.getLocalWebserviceClient().withRetryForever(60000)
					.create(questionnaireResponse);
			execution.setVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_ID,
					created.getIdElement().getIdPart());

			logger.info("Created QuestionnaireResponse for user task at {}, process waiting for it's completion",
					created.getIdElement().toVersionless().withServerBase(clientProvider.getLocalBaseUrl(),
							ResourceType.QuestionnaireResponse.name()));

			afterQuestionnaireResponseCreate(userTask, created);
		}
		catch (Exception exception)
		{
			Task task = getTask(execution);

			logger.debug("Error while executing user task listener " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(),
					task.getIdElement().toVersionless()
							.withServerBase(clientProvider.getLocalBaseUrl(), ResourceType.Task.name()).getValue(),
					exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(taskHelper.createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					errorMessage));
			task.setStatus(Task.TaskStatus.FAILED);

			clientProvider.getLocalWebserviceClient().withMinimalReturn().update(task);

			// TODO evaluate throwing exception as alternative to stopping the process instance
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
	}

	private Questionnaire readQuestionnaire(String urlWithVersion)
	{
		Bundle search = clientProvider.getLocalWebserviceClient().search(Questionnaire.class,
				Map.of("url", Collections.singletonList(urlWithVersion)));

		List<Questionnaire> questionnaires = search.getEntry().stream().filter(Bundle.BundleEntryComponent::hasResource)
				.map(Bundle.BundleEntryComponent::getResource).filter(r -> r instanceof Questionnaire)
				.map(r -> (Questionnaire) r).collect(Collectors.toList());

		if (questionnaires.size() < 1)
			throw new RuntimeException("Could not find Questionnaire resource with url|version=" + urlWithVersion);

		if (questionnaires.size() > 1)
			logger.info("Found {} Questionnaire resources with url|version={}, using the first", questionnaires.size(),
					urlWithVersion);

		return questionnaires.get(0);
	}

	private QuestionnaireResponse createDefaultQuestionnaireResponse(String questionnaireUrlWithVersion,
			String businessKey, String userTaskId)
	{
		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
		questionnaireResponse.setQuestionnaire(questionnaireUrlWithVersion);
		questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);

		questionnaireResponse.setAuthor(new Reference().setType(ResourceType.Organization.name())
				.setIdentifier(organizationProvider.getLocalIdentifier()));

		if (addBusinessKeyToQuestionnaireResponse())
		{
			questionnaireResponseHelper.addItemLeafWithAnswer(questionnaireResponse,
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY, "The business-key of the process execution",
					new StringType(businessKey));
		}

		questionnaireResponseHelper.addItemLeafWithAnswer(questionnaireResponse,
				CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, "The user-task-id of the process execution",
				new StringType(userTaskId));

		return questionnaireResponse;
	}

	private void transformQuestionnaireItemsToQuestionnaireResponseItems(QuestionnaireResponse questionnaireResponse,
			Questionnaire questionnaire)
	{
		questionnaire.getItem().stream()
				.filter(i -> !CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(i.getLinkId()))
				.filter(i -> !CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID.equals(i.getLinkId()))
				.forEach(i -> transformItem(questionnaireResponse, i));
	}

	private void transformItem(QuestionnaireResponse questionnaireResponse,
			Questionnaire.QuestionnaireItemComponent question)
	{
		if (Questionnaire.QuestionnaireItemType.DISPLAY.equals(question.getType()))
		{
			questionnaireResponseHelper.addItemLeafWithoutAnswer(questionnaireResponse, question.getLinkId(),
					question.getText());
		}
		else
		{
			Type answer = questionnaireResponseHelper.transformQuestionTypeToAnswerType(question);
			questionnaireResponseHelper.addItemLeafWithAnswer(questionnaireResponse, question.getLinkId(),
					question.getText(), answer);
		}
	}

	private void checkQuestionnaireResponse(QuestionnaireResponse questionnaireResponse)
	{
		if (addBusinessKeyToQuestionnaireResponse())
		{
			questionnaireResponse.getItem().stream()
					.filter(i -> CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(i.getLinkId())).findFirst()
					.orElseThrow(
							() -> new RuntimeException("QuestionnaireResponse does not contain an item with linkId='"
									+ CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY + "'"));
		}

		questionnaireResponse.getItem().stream()
				.filter(i -> CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID.equals(i.getLinkId())).findFirst()
				.orElseThrow(() -> new RuntimeException("QuestionnaireResponse does not contain an item with linkId='"
						+ CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID + "'"));

		if (!QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS.equals(questionnaireResponse.getStatus()))
			throw new RuntimeException("QuestionnaireResponse must be in status 'in-progress'");
	}

	/**
	 * <i>Override this method to decided if you want to add the Business-Key to the {@link QuestionnaireResponse}
	 * resource as an item with {@link QuestionnaireResponseItemComponent#getLinkId()} equal to
	 * {@link ConstantsBase#CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY}</i>
	 *
	 * @return <code>false</code>
	 */
	protected boolean addBusinessKeyToQuestionnaireResponse()
	{
		return false;
	}

	/**
	 * <i>Override this method to modify the {@link QuestionnaireResponse} before it will be created in state
	 * {@link QuestionnaireResponse.QuestionnaireResponseStatus#INPROGRESS} on the DSF FHIR server</i>
	 *
	 * @param userTask
	 *            not <code>null</code>, user task on which this {@link QuestionnaireResponse} is based
	 * @param beforeCreate
	 *            not <code>null</code>, containing an answer placeholder for every item in the corresponding
	 *            {@link Questionnaire}
	 */
	protected void beforeQuestionnaireResponseCreate(DelegateTask userTask, QuestionnaireResponse beforeCreate)
	{
		// Nothing to do in default behavior
	}

	/**
	 * <i>Override this method to execute code after the {@link QuestionnaireResponse} resource has been created on the
	 * DSF FHIR server</i>
	 *
	 * @param userTask
	 *            not <code>null</code>, user task on which this {@link QuestionnaireResponse} is based
	 * @param afterCreate
	 *            not <code>null</code>, created on the DSF FHIR server
	 */
	protected void afterQuestionnaireResponseCreate(DelegateTask userTask, QuestionnaireResponse afterCreate)
	{
		// Nothing to do in default behavior
	}

	protected final TaskHelper getTaskHelper()
	{
		return taskHelper;
	}

	protected final FhirWebserviceClientProvider getFhirWebserviceClientProvider()
	{
		return clientProvider;
	}

	protected final ReadAccessHelper getReadAccessHelper()
	{
		return readAccessHelper;
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the active task from execution variables, i.e. the leading task if the main process is running or the
	 *         current task if a subprocess is running.
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getTask(DelegateExecution execution)
	{
		return taskHelper.getTask(execution);
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the current task from execution variables, the task resource that started the current process or
	 *         subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final Task getCurrentTaskFromExecutionVariables(DelegateExecution execution)
	{
		return taskHelper.getCurrentTaskFromExecutionVariables(execution);
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @return the leading task from execution variables, same as current task if not in a subprocess
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final Task getLeadingTaskFromExecutionVariables(DelegateExecution execution)
	{
		return taskHelper.getLeadingTaskFromExecutionVariables(execution);
	}

	/**
	 * <i>Use this method to update the process engine variable {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK},
	 * after modifying the {@link Task}.</i>
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_TASK
	 */
	protected final void updateCurrentTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		taskHelper.updateCurrentTaskInExecutionVariables(execution, task);
	}

	/**
	 * <i>Use this method to update the process engine variable
	 * {@link ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK}, after modifying the {@link Task}.</i>
	 * <p>
	 * Updates the current task if no leading task is set.
	 *
	 * @param execution
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 * @throws IllegalStateException
	 *             if execution of this service delegate has not been started
	 * @see ConstantsBase#BPMN_EXECUTION_VARIABLE_LEADING_TASK
	 */
	protected final void updateLeadingTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		taskHelper.updateLeadingTaskInExecutionVariables(execution, task);
	}
}
