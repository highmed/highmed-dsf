package org.highmed.dsf.fhir.questionnaire;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Type;

public interface QuestionnaireResponseHelper
{
	Optional<QuestionnaireResponse.QuestionnaireResponseItemComponent> getFirstItemLeaveMatchingLinkId(
			QuestionnaireResponse questionnaireResponse, String linkId);

	List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsList(
			QuestionnaireResponse questionnaireResponse, String linkId);

	Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsStream(
			QuestionnaireResponse questionnaireResponse, String linkId);

	List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsList(
			QuestionnaireResponse questionnaireResponse);

	Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsStream(
			QuestionnaireResponse questionnaireResponse);

	void addItemLeave(QuestionnaireResponse questionnaireResponse, String linkId, Type answer);
}
