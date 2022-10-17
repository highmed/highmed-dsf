package org.highmed.dsf.fhir.questionnaire;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_COMPLETED;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.TaskService;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class QuestionnaireResponseHandler implements ResourceHandler<QuestionnaireResponse>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseHandler.class);

	private final TaskService userTaskService;

	public QuestionnaireResponseHandler(TaskService userTaskService)
	{
		this.userTaskService = userTaskService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(userTaskService, "taskService");
	}

	public void onResource(QuestionnaireResponse questionnaireResponse)
	{
		try
		{
			List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = questionnaireResponse.getItem();

			String questionnaireResponseId = questionnaireResponse.getId();
			String questionnaire = questionnaireResponse.getQuestionnaire();
			String user = questionnaireResponse.getAuthor().getIdentifier().getValue();
			String userType = questionnaireResponse.getAuthor().getType();
			String businessKey = getStringValueFromItems(items, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY,
					questionnaireResponseId).orElse("?");

			Optional<String> userTaskIdOpt = getStringValueFromItems(items,
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, questionnaireResponseId);

			userTaskIdOpt.ifPresentOrElse(userTaskId ->
			{
				logger.info(
						"QuestionnaireResponse '{}' for Questionnaire '{}' completed [userTaskId: {}, businessKey: {}, user: {}]",
						questionnaireResponseId, questionnaire, userTaskId, businessKey, user + "|" + userType);

				Map<String, Object> variables = Map.of(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_RESPONSE_COMPLETED,
						FhirResourceValues.create(questionnaireResponse));
				userTaskService.complete(userTaskId, variables);
			}, () ->
			{
				logger.warn(
						"QuestionnaireResponse '{}' for Questionnaire '{}' has no answer with item.linkId '{}' [businessKey: {}, user: {}], ignoring QuestionnaireResponse",
						questionnaireResponseId, questionnaire, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID,
						businessKey, user + "|" + userType);
			});
		}
		catch (Exception e)
		{
			logger.warn("Unable to complete UserTask", e);
			throw new RuntimeException(e);
		}
	}

	private Optional<String> getStringValueFromItems(
			List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items, String linkId,
			String questionnaireResponseId)
	{
		List<String> answers = items.stream().filter(i -> linkId.equals(i.getLinkId()))
				.flatMap(i -> i.getAnswer().stream()).filter(a -> a.getValue() instanceof StringType)
				.map(a -> ((StringType) a.getValue()).getValue()).collect(Collectors.toList());

		if (answers.size() == 0)
		{
			logger.info("QuestionnaireResponse with id '{}' did not contain any linkId '{}'", questionnaireResponseId,
					linkId);
			return Optional.empty();
		}

		if (answers.size() > 1)
			logger.warn("QuestionnaireResponse with id '{}' contained {} linkIds '{}', using the first",
					questionnaireResponseId, answers.size(), linkId);

		return Optional.of(answers.get(0));
	}
}
