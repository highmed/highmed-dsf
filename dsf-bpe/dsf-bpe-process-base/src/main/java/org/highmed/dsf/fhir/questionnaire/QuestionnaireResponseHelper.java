package org.highmed.dsf.fhir.questionnaire;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Type;

public interface QuestionnaireResponseHelper
{
	default Optional<QuestionnaireResponse.QuestionnaireResponseItemComponent> getFirstItemLeaveMatchingLinkId(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesMatchingLinkIdAsStream(questionnaireResponse, linkId).findFirst();
	}

	default List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsList(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesMatchingLinkIdAsStream(questionnaireResponse, linkId).collect(Collectors.toList());
	}

	Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsStream(
			QuestionnaireResponse questionnaireResponse, String linkId);

	default List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsList(
			QuestionnaireResponse questionnaireResponse)
	{
		return getItemLeavesAsStream(questionnaireResponse).collect(Collectors.toList());
	}

	Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsStream(
			QuestionnaireResponse questionnaireResponse);

	Type transformQuestionTypeToAnswerType(Questionnaire.QuestionnaireItemComponent question);

	void addItemLeafWithoutAnswer(QuestionnaireResponse questionnaireResponse, String linkId, String text);

	void addItemLeafWithAnswer(QuestionnaireResponse questionnaireResponse, String linkId, String text, Type answer);
}
