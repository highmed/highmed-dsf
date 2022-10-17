package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Test;

public class QuestionnaireResponseIntegrationTest extends AbstractIntegrationTest
{
	private static final Date AUTHORED = Date
			.from(LocalDateTime.parse("2022-01-01T00:00:00").toInstant(ZoneOffset.UTC));
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

	@Test(expected = WebApplicationException.class)
	public void testCreateNotAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		questionnaireResponse.setStatus(QuestionnaireResponseStatus.COMPLETED);

		try
		{
			getWebserviceClient().create(questionnaireResponse);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();

		try
		{
			getExternalWebserviceClient().create(questionnaireResponse);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testUpdateAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		created.setStatus(QuestionnaireResponseStatus.COMPLETED);
		QuestionnaireResponse updated = getWebserviceClient().update(created);

		assertNotNull(updated);
		assertNotNull(updated.getIdElement().getIdPart());
		assertEquals(created.getIdElement().getIdPart(), updated.getIdElement().getIdPart());
		assertNotNull(updated.getIdElement().getVersionIdPart());
		assertEquals("2", updated.getIdElement().getVersionIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		try
		{
			getWebserviceClient().update(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedByLocalUserNowUserTaskId() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		created.setStatus(QuestionnaireResponseStatus.STOPPED);
		created.getItem().clear();

		try
		{
			getWebserviceClient().update(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedByLocalUserChangedUserTaskId() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		created.setStatus(QuestionnaireResponseStatus.STOPPED);
		created.getItem().clear();
		addItem(created, ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, "UserTask ID",
				new StringType(UUID.randomUUID().toString()));

		try
		{
			getWebserviceClient().update(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testSecondUpdateNotAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);
		created.setStatus(QuestionnaireResponseStatus.COMPLETED);
		QuestionnaireResponse updated = questionnaireResponseDao.update(created);

		try
		{
			getWebserviceClient().update(updated);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		created.setStatus(QuestionnaireResponseStatus.COMPLETED);
		try
		{
			getExternalWebserviceClient().update(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testSearchByDate() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
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
		final String value = UUID.randomUUID().toString();
		final String system = "http://foo/fhir/sid/Test";

		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		questionnaireResponse.getIdentifier().setSystem(system).setValue(value);
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("identifier", Collections.singletonList(system + "|" + value)));

		assertNotNull(searchBundle.getEntry());
		assertEquals(1, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		QuestionnaireResponse foundQuestionnaireResponse = (QuestionnaireResponse) searchBundle.getEntry().get(0)
				.getResource();
		assertTrue(foundQuestionnaireResponse.hasIdentifier());
	}

	@Test
	public void testSearchByIdentifierRemoteUser() throws Exception
	{
		final String value = UUID.randomUUID().toString();
		final String system = "http://foo/fhir/sid/Test";

		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		questionnaireResponse.getIdentifier().setSystem(system).setValue(value);
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		Bundle searchBundle = getExternalWebserviceClient().search(QuestionnaireResponse.class,
				Map.of("identifier", Collections.singletonList(system + "|" + value)));

		assertNotNull(searchBundle.getEntry());
		assertEquals(0, searchBundle.getEntry().size());
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
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
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
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
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
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
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
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
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
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		questionnaireResponseDao.create(questionnaireResponse);

		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
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
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

		questionnaireResponse.setQuestionnaire(QUESTIONNAIRE);

		questionnaireResponse.setStatus(STATUS);
		questionnaireResponse.setAuthored(AUTHORED);

		String organizationReference = "Organization/"
				+ organizationProvider.getLocalOrganization().get().getIdElement().getIdPart();
		questionnaireResponse.setSubject(new Reference(organizationReference));

		addItem(questionnaireResponse, ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID,
				"UserTask ID", new StringType(UUID.randomUUID().toString()));

		return questionnaireResponse;
	}

	private void addItem(QuestionnaireResponse questionnaireResponse, String linkId, String text, Type answer)
	{
		List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answerComponent = Collections
				.singletonList(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(answer));

		questionnaireResponse.addItem().setLinkId(linkId).setText(text).setAnswer(answerComponent);
	}

	@Test
	public void testDeleteAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		getWebserviceClient().delete(QuestionnaireResponse.class, created.getIdElement().getIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testDeleteNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		try
		{
			getExternalWebserviceClient().delete(QuestionnaireResponse.class, created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testReadAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		QuestionnaireResponse read = getWebserviceClient().read(QuestionnaireResponse.class,
				created.getIdElement().getIdPart());

		assertNotNull(read);
		assertNotNull(read.getIdElement().getIdPart());
		assertEquals(created.getIdElement().getIdPart(), read.getIdElement().getIdPart());
		assertNotNull(read.getIdElement().getVersionIdPart());
		assertEquals("1", read.getIdElement().getVersionIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testReadNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		try
		{
			getExternalWebserviceClient().read(QuestionnaireResponse.class, created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testReadNotAllowedByRemoteUserWithVersion() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		try
		{
			getExternalWebserviceClient().read(QuestionnaireResponse.class, created.getIdElement().getIdPart(),
					created.getIdElement().getVersionIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testNotModifiedCheckAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		QuestionnaireResponse read = getWebserviceClient().read(created);
		assertNotNull(read);
		assertTrue(created == read);
	}

	@Test(expected = WebApplicationException.class)
	public void testNotModifiedCheckNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);

		try
		{
			getExternalWebserviceClient().read(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testNotModifiedCheckAllowedByLocalUserWithModification() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);
		QuestionnaireResponse updated = questionnaireResponseDao.update(created);
		assertEquals("2", updated.getIdElement().getVersionIdPart());

		QuestionnaireResponse read = getWebserviceClient().read(created);
		assertNotNull(read);
		assertTrue(created != read);

		assertEquals("2", read.getIdElement().getVersionIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testNotModifiedCheckNotAllowedByRemoteUserWithModification() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);
		QuestionnaireResponse updated = questionnaireResponseDao.update(created);
		assertEquals("2", updated.getIdElement().getVersionIdPart());

		try
		{
			getExternalWebserviceClient().read(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testHistory() throws Exception
	{
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(createQuestionnaireResponse());

		Bundle historyBundle = getWebserviceClient().history(QuestionnaireResponse.class,
				created.getIdElement().getIdPart());

		assertNotNull(historyBundle.getEntry());
		assertEquals(1, historyBundle.getEntry().size());
		assertNotNull(historyBundle.getEntry().get(0));
		assertNotNull(historyBundle.getEntry().get(0).getResource());
		assertTrue(historyBundle.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		Bundle historyBundle2 = getWebserviceClient().history(QuestionnaireResponse.class);

		assertNotNull(historyBundle2.getEntry());
		assertEquals(1, historyBundle2.getEntry().size());
		assertNotNull(historyBundle2.getEntry().get(0));
		assertNotNull(historyBundle2.getEntry().get(0).getResource());
		assertTrue(historyBundle2.getEntry().get(0).getResource() instanceof QuestionnaireResponse);

		Bundle historyBundle3 = getWebserviceClient().history(1, Integer.MAX_VALUE);

		assertNotNull(historyBundle3.getEntry());

		List<QuestionnaireResponse> qrFromBundle = historyBundle3.getEntry().stream()
				.filter(e -> e.hasResource() && e.getResource() instanceof QuestionnaireResponse)
				.map(e -> (QuestionnaireResponse) e.getResource()).collect(Collectors.toList());

		assertEquals(1, qrFromBundle.size());
		assertNotNull(qrFromBundle.get(0));
	}

	@Test
	public void testHistoryRemoteUser() throws Exception
	{
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(createQuestionnaireResponse());

		Bundle historyBundle = getExternalWebserviceClient().history(QuestionnaireResponse.class,
				created.getIdElement().getIdPart());

		assertNotNull(historyBundle.getEntry());
		assertEquals(0, historyBundle.getEntry().size());

		Bundle historyBundle2 = getExternalWebserviceClient().history(QuestionnaireResponse.class);

		assertNotNull(historyBundle2.getEntry());
		assertEquals(0, historyBundle2.getEntry().size());

		Bundle historyBundle3 = getExternalWebserviceClient().history(1, Integer.MAX_VALUE);

		assertNotNull(historyBundle3.getEntry());
		assertNotSame(0, historyBundle3.getEntry().size());

		List<QuestionnaireResponse> qrFromBundle = historyBundle3.getEntry().stream()
				.filter(e -> e.hasResource() && e.getResource() instanceof QuestionnaireResponse)
				.map(e -> (QuestionnaireResponse) e.getResource()).collect(Collectors.toList());

		assertEquals(0, qrFromBundle.size());
	}

	@Test
	public void testDeletePermanentlyAllowedByLocalUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);
		questionnaireResponseDao.delete(UUID.fromString(created.getIdElement().getIdPart()));

		getWebserviceClient().deletePermanently(QuestionnaireResponse.class, created.getIdElement().getIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testDeletePermanentlyNotAllowedByRemoteUser() throws Exception
	{
		QuestionnaireResponse questionnaireResponse = createQuestionnaireResponse();
		QuestionnaireResponseDao questionnaireResponseDao = getSpringWebApplicationContext()
				.getBean(QuestionnaireResponseDao.class);
		QuestionnaireResponse created = questionnaireResponseDao.create(questionnaireResponse);
		questionnaireResponseDao.delete(UUID.fromString(created.getIdElement().getIdPart()));

		try
		{
			getExternalWebserviceClient().deletePermanently(QuestionnaireResponse.class,
					created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}
}
