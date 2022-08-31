package org.highmed.dsf.bpe.listener;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_ID;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_VERSION;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.highmed.dsf.bpe.AbstractDelegateAndListener;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractUserTaskListener extends AbstractDelegateAndListener
		implements TaskListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractUserTaskListener.class);

	private final OrganizationProvider organizationProvider;
	private final QuestionnaireResponseHelper questionnaireResponseHelper;

	protected DelegateExecution execution;

	public AbstractUserTaskListener(FhirWebserviceClientProvider clientProvider,
			OrganizationProvider organizationProvider, QuestionnaireResponseHelper questionnaireResponseHelper,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
		this.questionnaireResponseHelper = questionnaireResponseHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(questionnaireResponseHelper, "questionnaireResponseHelper");
	}

	@Override
	public void notify(DelegateTask userTask)
	{
		setExecution(userTask.getExecution());

		try
		{
			String questionnaireUrl = (String) userTask.getExecution()
					.getVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL);
			String questionnaireVersion = (String) userTask.getExecution()
					.getVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_VERSION);
			Questionnaire questionnaire = readQuestionnaire(questionnaireUrl, questionnaireVersion);

			String businessKey = userTask.getExecution().getBusinessKey();
			String userTaskId = userTask.getId();

			QuestionnaireResponse questionnaireResponse = createDefaultQuestionnaireResponse(questionnaireUrl,
					questionnaireVersion, businessKey, userTaskId);
			addPlaceholderAnswersToQuestionnaireResponse(questionnaireResponse, questionnaire);

			modifyQuestionnaireResponse(userTask, questionnaireResponse);

			checkQuestionnaireResponse(questionnaireResponse);

			IdType created = getFhirWebserviceClientProvider().getLocalWebserviceClient().withRetryForever(60000)
					.create(questionnaireResponse).getIdElement();
			userTask.getExecution().setVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_ID + userTask.getId(),
					created.getIdPart());

			logger.info("Created user task with id={}, process waiting for it's completion", created.getValue());
		}
		// Error boundary event, do not stop process execution
		catch (BpmnError error)
		{
			Task task = getTask();

			logger.debug("Error while executing user task listener " + getClass().getName(), error);
			logger.error(
					"Process {} encountered error boundary event in step {} for task with id {}, error-code: {}, message: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					error.getErrorCode(), error.getMessage());

			throw error;
		}
		// Not an error boundary event, stop process execution
		catch (Exception exception)
		{
			Task task = getTask();

			logger.debug("Error while executing user task listener " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task with id {}, reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(), task.getId(),
					exception.getMessage());

			String errorMessage = "Process " + execution.getProcessDefinitionId() + " has fatal error in step "
					+ execution.getActivityInstanceId() + ", reason: " + exception.getMessage();

			task.addOutput(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					errorMessage));
			task.setStatus(Task.TaskStatus.FAILED);

			getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);

			// TODO evaluate throwing exception as alternative to stopping the process instance
			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}
	}

	private Questionnaire readQuestionnaire(String url, String version)
	{
		Bundle search = getFhirWebserviceClientProvider().getLocalWebserviceClient().search(Questionnaire.class,
				Map.of("url", Collections.singletonList(url), "version", Collections.singletonList(version)));

		List<Questionnaire> questionnaires = search.getEntry().stream().filter(Bundle.BundleEntryComponent::hasResource)
				.map(Bundle.BundleEntryComponent::getResource).filter(r -> r instanceof Questionnaire)
				.map(r -> (Questionnaire) r).collect(Collectors.toList());

		if (questionnaires.size() < 1)
			throw new RuntimeException("Could not find Questionnaire resource with url|version=" + url + "|" + version);

		if (questionnaires.size() > 1)
			logger.info("Found {} Questionnaire resources with url|version={}|{}, using the first",
					questionnaires.size(), version, url);

		return questionnaires.get(0);
	}

	private QuestionnaireResponse createDefaultQuestionnaireResponse(String questionnaireUrl,
			String questionnaireVersion, String businessKey, String userTaskId)
	{
		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
		questionnaireResponse.setQuestionnaire(questionnaireUrl + "|" + questionnaireVersion);
		questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);

		questionnaireResponse.setAuthor(new Reference().setType(ResourceType.Organization.name())
				.setIdentifier(organizationProvider.getLocalIdentifier()));

		questionnaireResponseHelper.addItemLeave(questionnaireResponse,
				CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY, "The business-key of the process execution",
				new StringType(businessKey));
		questionnaireResponseHelper.addItemLeave(questionnaireResponse,
				CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, "The user-task-id of the process execution",
				new StringType(userTaskId));

		getReadAccessHelper().addLocal(questionnaireResponse);

		return questionnaireResponse;
	}

	private void addPlaceholderAnswersToQuestionnaireResponse(QuestionnaireResponse questionnaireResponse,
			Questionnaire questionnaire)
	{
		questionnaire.getItem().stream()
				.filter(i -> !CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(i.getLinkId()))
				.filter(i -> !CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID.equals(i.getLinkId()))
				.forEach(i -> createAndAddAnswerPlaceholder(questionnaireResponse, i));
	}

	private void createAndAddAnswerPlaceholder(QuestionnaireResponse questionnaireResponse,
			Questionnaire.QuestionnaireItemComponent question)
	{
		Type answer = questionnaireResponseHelper.transformQuestionTypeToAnswerType(question);
		questionnaireResponseHelper.addItemLeave(questionnaireResponse, question.getLinkId(), question.getText(),
				answer);
	}

	private void checkQuestionnaireResponse(QuestionnaireResponse questionnaireResponse)
	{
		questionnaireResponse.getItem().stream()
				.filter(i -> CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(i.getLinkId())).findFirst()
				.orElseThrow(() -> new RuntimeException("QuestionnaireResponse does not contain an item with linkId='"
						+ CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY + "'"));

		questionnaireResponse.getItem().stream()
				.filter(i -> CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID.equals(i.getLinkId())).findFirst()
				.orElseThrow(() -> new RuntimeException("QuestionnaireResponse does not contain an item with linkId='"
						+ CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID + "'"));

		if (!QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS.equals(questionnaireResponse.getStatus()))
			throw new RuntimeException("QuestionnaireResponse must be in status 'in-progress'");
	}

	/**
	 * Use this method to modify the {@link QuestionnaireResponse} before it will be created in state
	 * {@link QuestionnaireResponse.QuestionnaireResponseStatus#INPROGRESS}
	 *
	 * @param userTask
	 *            not <code>null</code>, user task on which this {@link QuestionnaireResponse} is based
	 * @param questionnaireResponse
	 *            not <code>null</code>, containing an answer placeholder for every item in the corresponding
	 *            {@link Questionnaire}
	 */
	protected void modifyQuestionnaireResponse(DelegateTask userTask, QuestionnaireResponse questionnaireResponse)
	{
	}
}
