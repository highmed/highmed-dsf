package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class QuestionnaireResponseIntegrationTest extends AbstractIntegrationTest
{
	private static final Date AUTHORED = Date.from(
			LocalDateTime.parse("2022-01-01T00:00:00").toInstant(ZoneOffset.UTC));
	private static final String IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/user-task-id";
	private static final String IDENTIFIER_VALUE = "foo";
	private static final String QUESTIONNAIRE = "http://highmed.org/fhir/Questionnaire/userTask/foo";
	private static final QuestionnaireResponse.QuestionnaireResponseStatus STATUS = QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS;

	private QuestionnaireResponse createQuestionnaireResponse()
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext().getBean(
				OrganizationProvider.class);
		assertNotNull(organizationProvider);

		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
		questionnaireResponse.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag")
				.setCode("ALL");

		questionnaireResponse.getIdentifier().setSystem(IDENTIFIER_SYSTEM).setValue(IDENTIFIER_VALUE);

		questionnaireResponse.setQuestionnaire(QUESTIONNAIRE);

		questionnaireResponse.setStatus(STATUS);
		questionnaireResponse.setAuthored(AUTHORED);

		questionnaireResponse.setSubject(new Reference(organizationProvider.getLocalOrganization().get()));

		questionnaireResponse.addItem().setLinkId("foo").setText("Approve?").addAnswer()
				.setValue(new BooleanType(true));

		return questionnaireResponse;
	}

	@Test
	public void testCreateValidByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();

		QuestionnaireResponse created = getWebserviceClient().create(questionnaireResponse);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testSearchByDate() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("authored", Collections.singletonList("le2022-02-01")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaireResponse.hasAuthored());
		assertEquals(0, AUTHORED.compareTo(searchQuestionnaireResponse.getAuthored()));
	}

	@Test
	public void testSearchByIdentifier() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("identifier", Collections.singletonList(IDENTIFIER_SYSTEM + "|" + IDENTIFIER_VALUE)));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaireResponse.hasIdentifier());
		assertEquals(IDENTIFIER_SYSTEM, searchQuestionnaireResponse.getIdentifier().getSystem());
		assertEquals(IDENTIFIER_VALUE, searchQuestionnaireResponse.getIdentifier().getValue());
	}

	@Test
	public void testSearchByStatus() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("status", Collections.singletonList(STATUS.toCode())));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaireResponse.hasStatus());
		assertEquals(STATUS, searchQuestionnaireResponse.getStatus());
	}
}
