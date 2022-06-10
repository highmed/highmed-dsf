package org.highmed.dsf.fhir.questionnaire;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_TASK_ID;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.TaskService;
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
			String user = questionnaireResponse.getSubject().getIdentifier().getValue();
			String userType = questionnaireResponse.getSubject().getType();
			String businessKey = getStringValueFromItems(items, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY,
					questionnaireResponseId)
							.orElseThrow(() -> new RuntimeException(
									"Missing linkId " + CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY));
			String taskId = getStringValueFromItems(items, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_TASK_ID,
					questionnaireResponseId)
							.orElseThrow(() -> new RuntimeException(
									"Missing linkId " + CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_TASK_ID));

			logger.info("User task '{}' for Questionnaire '{}' completed [userTaskId: {}, businessKey: {}, user: {}]",
					questionnaireResponseId, questionnaire, taskId, businessKey, user + "|" + userType);
			userTaskService.complete(taskId);

			// TODO: add remaining items as process variables using linkId as key
		}
		catch (Exception exception)
		{
			// TODO handle exception
			throw new RuntimeException(exception);
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
			logger.warn("QuestionnaireResponse with id '{}' did not contain any linkId '{}'", questionnaireResponseId,
					linkId);
			return Optional.empty();
		}

		if (answers.size() > 1)
			logger.warn("QuestionnaireResponse with id '{}' contained {} linkIds '{}', using the first",
					questionnaireResponseId, answers.size(), linkId);

		return Optional.of(answers.get(0));
	}
}
