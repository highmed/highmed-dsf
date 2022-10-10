package org.highmed.dsf.fhir.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import org.junit.Test;

public class QuestionnaireResponseTest
{
	@Test
	public void testFlattenItemsToLeaves()
	{
		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

		QuestionnaireResponse.QuestionnaireResponseItemComponent node1 = questionnaireResponse.addItem();
		node1.setLinkId("Item 1.1 LinkId").addAnswer().setValue(new StringType("Item 1.1 Value"));

		QuestionnaireResponse.QuestionnaireResponseItemComponent node2 = questionnaireResponse.addItem();
		node2.addItem().setLinkId("Item 2.1 LinkId").addAnswer().setValue(new StringType("Item 2.1 Value"));
		node2.addItem().setLinkId("Item 2.2 LinkId").addAnswer().setValue(new StringType("Item 2.2 Value"));

		QuestionnaireResponse.QuestionnaireResponseItemComponent node3 = questionnaireResponse.addItem();
		node3.addItem().addItem().setLinkId("Item 3.1 LinkId").addAnswer().setValue(new StringType("Item 3.1 Value"));

		QuestionnaireResponseHelper questionnaireResponseHelper = new QuestionnaireResponseHelperImpl();

		List<QuestionnaireResponse.QuestionnaireResponseItemComponent> itemLeavesAsList = questionnaireResponseHelper
				.getItemLeavesAsList(questionnaireResponse);

		assertEquals(4, itemLeavesAsList.size());
	}

	@Test
	public void testQuestionTypeStringToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.STRING);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof StringType);
	}

	@Test
	public void testQuestionTypeTextToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.TEXT);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof StringType);
	}

	@Test
	public void testQuestionTypeBooleanToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.BOOLEAN);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof BooleanType);
	}

	@Test
	public void testQuestionTypeDecimalToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.DECIMAL);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof DecimalType);
	}

	@Test
	public void testQuestionTypeIntegerToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.INTEGER);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof IntegerType);
	}

	@Test
	public void testQuestionTypeDateToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.DATE);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof DateType);
	}

	@Test
	public void testQuestionTypeDateTimeToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.DATETIME);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof DateTimeType);
	}

	@Test
	public void testQuestionTypeTimeToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.TIME);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof TimeType);
	}

	@Test
	public void testQuestionTypeUrlToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.URL);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof UriType);
	}

	@Test
	public void testQuestionTypeReferenceToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.REFERENCE);

		Type type = qrh.transformQuestionTypeToAnswerType(question);

		assertTrue(type instanceof Reference);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeChoiceToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.CHOICE);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeOpenChoiceToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.OPENCHOICE);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeQuantityToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.QUANTITY);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeAttachmentToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.ATTACHMENT);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeGroupToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.GROUP);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeDisplayToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.DISPLAY);

		qrh.transformQuestionTypeToAnswerType(question);
	}

	@Test(expected = RuntimeException.class)
	public void testQuestionTypeQuestionToAnswerType()
	{
		QuestionnaireResponseHelper qrh = new QuestionnaireResponseHelperImpl();

		Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent()
				.setType(Questionnaire.QuestionnaireItemType.QUESTION);

		qrh.transformQuestionTypeToAnswerType(question);
	}
}
