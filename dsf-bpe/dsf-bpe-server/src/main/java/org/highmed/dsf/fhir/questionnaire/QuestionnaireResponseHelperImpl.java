package org.highmed.dsf.fhir.questionnaire;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Type;

public class QuestionnaireResponseHelperImpl implements QuestionnaireResponseHelper
{
	@Override
	public Optional<QuestionnaireResponse.QuestionnaireResponseItemComponent> getFirstItemLeaveMatchingLinkId(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesMatchingLinkIdAsStream(questionnaireResponse, linkId).findFirst();
	}

	@Override
	public List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsList(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesMatchingLinkIdAsStream(questionnaireResponse, linkId).collect(Collectors.toList());
	}

	@Override
	public Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsStream(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesAsStream(questionnaireResponse).filter(i -> linkId.equals(i.getLinkId()));
	}

	@Override
	public List<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsList(
			QuestionnaireResponse questionnaireResponse)
	{
		return getItemLeavesAsStream(questionnaireResponse).collect(Collectors.toList());
	}

	@Override
	public Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesAsStream(
			QuestionnaireResponse questionnaireResponse)
	{
		return flatItems(questionnaireResponse.getItem());
	}

	private Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> flatItems(
			List<QuestionnaireResponse.QuestionnaireResponseItemComponent> toFlat)
	{
		return toFlat.stream().flatMap(this::leaves);
	}

	private Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> leaves(
			QuestionnaireResponse.QuestionnaireResponseItemComponent component)
	{
		if (component.getItem().size() > 0)
			return component.getItem().stream().flatMap(this::leaves);
		else
			return Stream.of(component);
	}

	@Override
	public void addItemLeave(QuestionnaireResponse questionnaireResponse, String linkId, Type answer)
	{
		questionnaireResponse.addItem().setLinkId(CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY).addAnswer()
				.setValue(answer);
	}
}
