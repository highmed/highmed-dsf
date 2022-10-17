package org.highmed.dsf.fhir.questionnaire;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;

public class QuestionnaireResponseHelperImpl implements QuestionnaireResponseHelper
{
	@Override
	public Stream<QuestionnaireResponse.QuestionnaireResponseItemComponent> getItemLeavesMatchingLinkIdAsStream(
			QuestionnaireResponse questionnaireResponse, String linkId)
	{
		return getItemLeavesAsStream(questionnaireResponse).filter(i -> linkId.equals(i.getLinkId()));
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
	public void addItemLeafWithAnswer(QuestionnaireResponse questionnaireResponse, String linkId, String text,
			Type answer)
	{
		List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answerComponent = Collections
				.singletonList(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(answer));

		questionnaireResponse.addItem().setLinkId(linkId).setText(text).setAnswer(answerComponent);
	}

	@Override
	public void addItemLeafWithoutAnswer(QuestionnaireResponse questionnaireResponse, String linkId, String text)
	{
		questionnaireResponse.addItem().setLinkId(linkId).setText(text);
	}

	@Override
	public Type transformQuestionTypeToAnswerType(Questionnaire.QuestionnaireItemComponent question)
	{
		switch (question.getType())
		{
			case STRING:
			case TEXT:
				return new StringType("Placeholder..");
			case INTEGER:
				return new IntegerType(0);
			case DECIMAL:
				return new DecimalType(0.00);
			case BOOLEAN:
				return new BooleanType(false);
			case DATE:
				return new DateType("1900-01-01");
			case TIME:
				return new TimeType("00:00:00");
			case DATETIME:
				return new DateTimeType("1900-01-01T00:00:00.000Z");
			case URL:
				return new UriType("http://example.org/foo");
			case REFERENCE:
				return new Reference("http://example.org/fhir/Placeholder/id");
			default:
				throw new RuntimeException("Type '" + question.getType().getDisplay()
						+ "' in Questionnaire.item is not supported as answer type");
		}
	}
}
