package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class QuestionnaireProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-questionnaire-0.9.0.xml"), Collections.emptyList(), Collections.emptyList());

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testQuestionnaireValidTypeString()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.STRING);
	}

	@Test
	public void testQuestionnaireValidTypeText()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.TEXT);
	}

	@Test
	public void testQuestionnaireValidTypeInteger()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.INTEGER);
	}

	@Test
	public void testQuestionnaireValidTypeDecimal()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.DECIMAL);
	}

	@Test
	public void testQuestionnaireValidTypeBoolean()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.BOOLEAN);
	}

	@Test
	public void testQuestionnaireValidTypeDate()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.DATE);
	}

	@Test
	public void testQuestionnaireValidTypeTime()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.TIME);
	}

	@Test
	public void testQuestionnaireValidTypeDateTime()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.DATETIME);
	}

	@Test
	public void testQuestionnaireValidTypeUrl()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.URL);
	}

	@Test
	public void testQuestionnaireValidTypeReference()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.REFERENCE);
	}

	@Test
	public void testQuestionnaireValidTypeDisplay()
	{
		testQuestionnaireValidType(Questionnaire.QuestionnaireItemType.DISPLAY);
	}

	private void testQuestionnaireValidType(Questionnaire.QuestionnaireItemType type)
	{
		Questionnaire res = createQuestionnaire(type);

		ValidationResult result = resourceValidator.validate(res);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testQuestionnaireInvalidTypeGroup()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.GROUP);
	}

	@Test
	public void testQuestionnaireInvalidTypeQuestion()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.QUESTION);
	}

	@Test
	public void testQuestionnaireInvalidTypeChoice()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.CHOICE);
	}

	@Test
	public void testQuestionnaireInvalidTypeOpenChoice()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.OPENCHOICE);
	}

	@Test
	public void testQuestionnaireInvalidTypeAttachment()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.ATTACHMENT);
	}

	@Test
	public void testQuestionnaireInvalidTypeQuantity()
	{
		testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType.QUANTITY);
	}

	private void testQuestionnaireInvalidType(Questionnaire.QuestionnaireItemType type)
	{
		Questionnaire res = createQuestionnaire(Questionnaire.QuestionnaireItemType.STRING);
		res.addItem().setLinkId("invalid-type").setType(type).setText("Invalid type");

		ValidationResult result = resourceValidator.validate(res);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(1,
				result.getMessages().stream()
						.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
								|| ResultSeverityEnum.FATAL.equals(m.getSeverity()))
						.filter(m -> m.getMessage() != null).filter(m -> m.getMessage().startsWith("type-code"))
						.count());
	}

	private Questionnaire createQuestionnaire(Questionnaire.QuestionnaireItemType type)
	{
		Questionnaire res = new Questionnaire();
		res.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/questionnaire");
		res.setUrl("http://highmed.org/fhir/Questionnaire/hello-world");
		res.setVersion("0.1.0");
		res.setDate(new Date());
		res.setStatus(Enumerations.PublicationStatus.ACTIVE);
		res.addItem().setLinkId("business-key").setType(Questionnaire.QuestionnaireItemType.STRING)
				.setText("The business-key of the process execution");
		res.addItem().setLinkId("user-task-id").setType(Questionnaire.QuestionnaireItemType.STRING)
				.setText("The user-task-id of the process execution");
		res.addItem().setLinkId("valid-type").setType(type).setText("valid type");

		return res;
	}
}
