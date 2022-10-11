package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.Test;

public class QuestionnaireIntegrationTest extends AbstractIntegrationTest
{
	private static final Date DATE = Date.from(LocalDateTime.parse("2022-01-01T00:00:00").toInstant(ZoneOffset.UTC));
	private static final String IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/user-task";
	private static final String IDENTIFIER_VALUE = "foo";
	private static final String URL = "http://highmed.org/fhir/Questionnaire/userTask/foo";
	private static final String VERSION = "1.0.0";
	private static final Enumerations.PublicationStatus STATUS = Enumerations.PublicationStatus.ACTIVE;

	private Questionnaire createQuestionnaire()
	{
		Questionnaire questionnaire = new Questionnaire();
		questionnaire.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");

		questionnaire.addIdentifier().setSystem(IDENTIFIER_SYSTEM).setValue(IDENTIFIER_VALUE);

		questionnaire.setUrl(URL);
		questionnaire.setVersion(VERSION);

		questionnaire.setStatus(STATUS);
		questionnaire.setDate(DATE);

		questionnaire.addItem().setLinkId("foo").setText("Approve?")
				.setType(Questionnaire.QuestionnaireItemType.BOOLEAN).addInitial().setValue(new BooleanType(false));

		return questionnaire;
	}

	@Test
	public void testCreateValidByLocalUser() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();

		Questionnaire created = getWebserviceClient().create(questionnaire);

		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}

	@Test
	public void testSearchByDate() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		Bundle searchBundle = getWebserviceClient().search(Questionnaire.class,
				Map.of("date", Collections.singletonList("le2022-02-01")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaire.hasDate());
		assertEquals(0, DATE.compareTo(searchQuestionnaire.getDate()));
	}

	@Test
	public void testSearchByIdentifier() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		Bundle searchBundle = getWebserviceClient().search(Questionnaire.class,
				Map.of("identifier", Collections.singletonList(IDENTIFIER_SYSTEM + "|" + IDENTIFIER_VALUE)));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(0).getResource();
		assertEquals(1, searchQuestionnaire.getIdentifier().size());
		assertEquals(IDENTIFIER_SYSTEM, searchQuestionnaire.getIdentifier().get(0).getSystem());
		assertEquals(IDENTIFIER_VALUE, searchQuestionnaire.getIdentifier().get(0).getValue());
	}

	@Test
	public void testSearchByStatus() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		Bundle searchBundle = getWebserviceClient().search(Questionnaire.class,
				Map.of("status", Collections.singletonList(STATUS.toCode())));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaire.hasStatus());
		assertEquals(STATUS, searchQuestionnaire.getStatus());
	}

	@Test
	public void testSearchByUrlAndVersion() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		Bundle searchBundle = getWebserviceClient().search(Questionnaire.class,
				Map.of("url", Collections.singletonList(URL), "version", Collections.singletonList(VERSION)));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(0).getResource();
		assertTrue(searchQuestionnaire.hasUrl());
		assertEquals(URL, searchQuestionnaire.getUrl());
		assertTrue(searchQuestionnaire.hasVersion());
		assertEquals(VERSION, searchQuestionnaire.getVersion());
	}
}
