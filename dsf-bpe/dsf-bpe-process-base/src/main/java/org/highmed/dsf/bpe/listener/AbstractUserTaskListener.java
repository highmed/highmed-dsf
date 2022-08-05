package org.highmed.dsf.bpe.listener;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_VERSION;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractUserTaskListener implements TaskListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractUserTaskListener.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;
	private final QuestionnaireResponseHelper questionnaireResponseHelper;
	private final ReadAccessHelper readAccessHelper;

	public AbstractUserTaskListener(FhirWebserviceClientProvider clientProvider,
			OrganizationProvider organizationProvider, QuestionnaireResponseHelper questionnaireResponseHelper,
			ReadAccessHelper readAccessHelper)
	{
		this.clientProvider = clientProvider;
		this.organizationProvider = organizationProvider;
		this.questionnaireResponseHelper = questionnaireResponseHelper;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(questionnaireResponseHelper, "questionnaireResponseHelper");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public void notify(DelegateTask userTask)
	{
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

			QuestionnaireResponse created = clientProvider.getLocalWebserviceClient().create(questionnaireResponse);
			logger.info("Created user task with id={}, process waiting for it's completion", created.getId());
		}
		catch (Exception exception)
		{
			// TODO implement
		}
	}

	private Questionnaire readQuestionnaire(String url, String version)
	{
		Bundle search = clientProvider.getLocalWebserviceClient().search(Questionnaire.class,
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
				CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY, "", new StringType(businessKey));
		questionnaireResponseHelper.addItemLeave(questionnaireResponse,
				CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, "", new StringType(userTaskId));

		readAccessHelper.addLocal(questionnaireResponse);

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

	protected void modifyQuestionnaireResponse(DelegateTask userTask, QuestionnaireResponse questionnaireResponse)
	{
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
	}
}
