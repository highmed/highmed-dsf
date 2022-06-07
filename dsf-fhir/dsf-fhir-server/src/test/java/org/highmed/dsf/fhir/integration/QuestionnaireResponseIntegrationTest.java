package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class QuestionnaireResponseIntegrationTest extends AbstractIntegrationTest
{
	private static final Date AUTHORED = Date.from(
			LocalDateTime.parse("2022-01-01T00:00:00").toInstant(ZoneOffset.UTC));
	private static final String IDENTIFIER_SYSTEM = "http://highmed.org/fhir/CodeSystem/user-task-id";
	private static final String IDENTIFIER_VALUE = "foo";
	private static final String QUESTIONNAIRE_URL = "http://highmed.org/fhir/Questionnaire/userTask/foo";
	private static final String QUESTIONNAIRE_VERSION = "1.0.0";
	private static final String QUESTIONNAIRE = QUESTIONNAIRE_URL + "|" + QUESTIONNAIRE_VERSION;
	private static final QuestionnaireResponse.QuestionnaireResponseStatus STATUS = QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS;

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

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
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

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasIdentifier());
		assertEquals(IDENTIFIER_SYSTEM, searchQuestionnaireResponse.getIdentifier().getSystem());
		assertEquals(IDENTIFIER_VALUE, searchQuestionnaireResponse.getIdentifier().getValue());
	}

	@Test
	public void testSearchByQuestionnaireWithVersion() throws Exception
	{
		testSearchByQuestionnaire(QUESTIONNAIRE);
	}

	@Test
	public void testSearchByQuestionnaireWithoutVersion() throws Exception
	{
		testSearchByQuestionnaire(QUESTIONNAIRE_URL);
	}

	private void testSearchByQuestionnaire(String questionnaireUrl) throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire();
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("questionnaire", Collections.singletonList(questionnaireUrl), "_include",
						Collections.singletonList("QuestionnaireResponse:questionnaire")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasQuestionnaire());
		assertEquals(QUESTIONNAIRE, searchQuestionnaireResponse.getQuestionnaire());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(1).getResource();
		assertTrue(searchQuestionnaire.hasUrl());
		assertEquals(QUESTIONNAIRE_URL, searchQuestionnaire.getUrl());
		assertTrue(searchQuestionnaire.hasVersion());
		assertEquals(QUESTIONNAIRE_VERSION, searchQuestionnaire.getVersion());
	}

	@Test
	public void testSearchByQuestionnaireNoVersion() throws Exception
	{
		Questionnaire questionnaire = createQuestionnaire().setVersion(null);
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);
		questionnaireDao.create(questionnaire);

		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse().setQuestionnaire(QUESTIONNAIRE_URL);
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("questionnaire", Collections.singletonList(QUESTIONNAIRE_URL), "_include",
						Collections.singletonList("QuestionnaireResponse:questionnaire")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasQuestionnaire());
		assertEquals(QUESTIONNAIRE_URL, searchQuestionnaireResponse.getQuestionnaire());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(1).getResource();
		assertTrue(searchQuestionnaire.hasUrl());
		assertEquals(QUESTIONNAIRE_URL, searchQuestionnaire.getUrl());
		assertFalse(searchQuestionnaire.hasVersion());
	}

	@Test
	public void testSearchByQuestionnaireWithoutVersionButMultipleVersionExist() throws Exception
	{
		QuestionnaireDao questionnaireDao = getSpringWebApplicationContext().getBean(QuestionnaireDao.class);

		Questionnaire questionnaire1 = createQuestionnaire().setVersion("0.1.0");
		questionnaireDao.create(questionnaire1);

		Questionnaire questionnaire2 = createQuestionnaire();
		questionnaireDao.create(questionnaire2);

		Questionnaire questionnaire3 = createQuestionnaire().setVersion("0.2.0");
		questionnaireDao.create(questionnaire3);

		Questionnaire questionnaire4 = createQuestionnaire().setVersion(null);
		questionnaireDao.create(questionnaire4);

		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("questionnaire", Collections.singletonList(QUESTIONNAIRE_URL), "_include",
						Collections.singletonList("QuestionnaireResponse:questionnaire")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasQuestionnaire());
		assertEquals(QUESTIONNAIRE, searchQuestionnaireResponse.getQuestionnaire());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Questionnaire);

		Questionnaire searchQuestionnaire = (Questionnaire) searchBundle.getEntry().get(1).getResource();
		assertTrue(searchQuestionnaire.hasUrl());
		assertEquals(QUESTIONNAIRE_URL, searchQuestionnaire.getUrl());

		// Expect newest version 1.0.0 (null, 0.1.0 and 0.2.0 exist as well)
		assertTrue(searchQuestionnaire.hasVersion());
		assertEquals(QUESTIONNAIRE_VERSION, searchQuestionnaire.getVersion());
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

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasStatus());
		assertEquals(STATUS, searchQuestionnaireResponse.getStatus());
	}

	@Test
	public void testSearchBySubjectReference() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext().getBean(
				QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		OrganizationProvider organizationProvider = getSpringWebApplicationContext().getBean(
				OrganizationProvider.class);
		Organization organization = organizationProvider.getLocalOrganization().get();

		String organizationReference = "Organization/" + organization.getIdElement().getIdPart();
		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("subject", Collections.singletonList(organizationReference), "_include",
						Collections.singletonList("QuestionnaireResponse:subject:Organization")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse searchQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(searchQuestionnaireResponse.hasStatus());
		assertEquals(organizationReference, searchQuestionnaireResponse.getSubject().getReference());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Organization);

		Organization searchOrganization = (Organization) searchBundle.getEntry().get(1).getResource();
		assertEquals(organization.getIdentifierFirstRep().getSystem(),
				searchOrganization.getIdentifierFirstRep().getSystem());
		assertEquals(organization.getIdentifierFirstRep().getValue(),
				searchOrganization.getIdentifierFirstRep().getValue());
	}

	private Questionnaire createQuestionnaire()
	{
		Questionnaire questionnaire = new Questionnaire();
		questionnaire.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");

		questionnaire.setUrl(QUESTIONNAIRE_URL);
		questionnaire.setVersion(QUESTIONNAIRE_VERSION);

		questionnaire.setStatus(Enumerations.PublicationStatus.ACTIVE);

		questionnaire.addItem().setLinkId("foo").setText("Approve?")
				.setType(Questionnaire.QuestionnaireItemType.BOOLEAN).addInitial().setValue(new BooleanType(false));

		return questionnaire;
	}

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

		String organizationReference =
				"Organization/" + organizationProvider.getLocalOrganization().get().getIdElement().getIdPart();
		questionnaireResponse.setSubject(new Reference(organizationReference));

		questionnaireResponse.addItem().setLinkId("foo").setText("Approve?").addAnswer()
				.setValue(new BooleanType(true));

		return questionnaireResponse;
	}
}
