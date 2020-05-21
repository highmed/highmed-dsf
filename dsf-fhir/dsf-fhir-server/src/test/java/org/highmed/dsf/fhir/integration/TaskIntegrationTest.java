package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskRestrictionComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class TaskIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskIntegrationTest.class);

	private List<Bundle.BundleEntryComponent> createTaskBundle()
	{
		Bundle bundle = readBundle(Paths.get("src/test/resources/integration/task-bundle.json"), newJsonParser());
		Bundle resultBundle = getWebserviceClient().postBundle(bundle);
		return resultBundle.getEntry();
	}

	@Test
	public void testHandleBundleForRequestSimpleFeasibility() throws Exception
	{
		WebsocketClient websocketClient = getWebsocketClient();
		assertNotNull(websocketClient);

		BlockingDeque<DomainResource> events = new LinkedBlockingDeque<>();
		websocketClient.setDomainResourceHandler(events::add, AbstractIntegrationTest::newJsonParser);
		websocketClient.connect();

		try
		{
			List<Bundle.BundleEntryComponent> resultBundleEntries = createTaskBundle();
			assertEquals(4, resultBundleEntries.size());

			String taskId = new IdType(resultBundleEntries.get(3).getFullUrl()).getIdPart();
			Task task = getWebserviceClient().read(Task.class, taskId);

			Task.ParameterComponent input = task.getInput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.ParameterComponent());

			IdType taskInputResearchStudyId = new IdType(((Reference) input.getValue()).getReference());
			IdType researchStudyId = new IdType(resultBundleEntries.get(2).getFullUrl());

			assertEquals(researchStudyId.getResourceType(), taskInputResearchStudyId.getResourceType());
			assertEquals(researchStudyId.getIdPart(), taskInputResearchStudyId.getIdPart());
			assertEquals(researchStudyId.getVersionIdPart(), taskInputResearchStudyId.getVersionIdPart());

			DomainResource event = events.pollFirst(5, TimeUnit.SECONDS);
			assertNotNull(event);
			assertTrue(event instanceof Task);

			Task taskViaWebsocket = (Task) event;
			Task.ParameterComponent inputViaWebsocket = taskViaWebsocket.getInput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.ParameterComponent());

			IdType taskInputResearchStudyIdViaWebsocket = new IdType(
					((Reference) inputViaWebsocket.getValue()).getReference());
			assertEquals(researchStudyId.getResourceType(), taskInputResearchStudyIdViaWebsocket.getResourceType());
			assertEquals(researchStudyId.getIdPart(), taskInputResearchStudyIdViaWebsocket.getIdPart());
			assertEquals(researchStudyId.getVersionIdPart(), taskInputResearchStudyIdViaWebsocket.getVersionIdPart());

			ResearchStudy researchStudy = getWebserviceClient().read(ResearchStudy.class, researchStudyId.getIdPart());
			logger.debug("ResearchStudy: {}",
					FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(researchStudy));

			List<Extension> medics = researchStudy
					.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/participating-medic");
			assertNotNull(medics);
			assertEquals(1, medics.size());
			Extension medicExt = medics.get(0);
			assertTrue(medicExt.hasValue());
			assertTrue(medicExt.getValue() instanceof Reference);
			Reference medicRef = (Reference) medicExt.getValue();
			assertTrue(medicRef.hasIdentifier());
			assertFalse(medicRef.hasReference());

			List<Extension> ttps = researchStudy
					.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp");
			assertNotNull(ttps);
			assertEquals(1, ttps.size());
			Extension ttpExt = medics.get(0);
			assertTrue(ttpExt.hasValue());
			assertTrue(ttpExt.getValue() instanceof Reference);
			Reference ttpRef = (Reference) ttpExt.getValue();
			assertTrue(ttpRef.hasIdentifier());
			assertFalse(ttpRef.hasReference());
		}
		finally
		{
			if (websocketClient != null)
				websocketClient.disconnect();
		}
	}

	@Test
	public void testCreateTaskStartPingProcess() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setStatus(TaskStatus.REQUESTED);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		getWebserviceClient().create(t);
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateTaskStartPingProcessNotAllowedForRemoteUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setStatus(TaskStatus.REQUESTED);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());

		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);

		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()));
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		try
		{
			getExternalWebserviceClient().create(t);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testCreateTaskStartPongProcessAllowedForRemoteUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setStatus(TaskStatus.REQUESTED);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());

		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);

		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()));
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		getExternalWebserviceClient().create(t);
	}

	@Test
	public void testCreateTaskContinuePingProcessAllowedForRemoteUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-pong");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setStatus(TaskStatus.REQUESTED);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());

		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);

		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()));
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pongMessage"));

		getExternalWebserviceClient().create(t);
	}

	@Test
	public void testUpdateTaskStartPingProcessStatusRequestedToInProgress() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setStatus(TaskStatus.REQUESTED);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference()
				.setReferenceElement(organizationProvider.getLocalOrganization().get().getIdElement().toVersionless());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		TaskDao dao = getSpringWebApplicationContext().getBean(TaskDao.class);
		Task created = dao.create(t);

		created.setStatus(TaskStatus.INPROGRESS);

		getWebserviceClient().update(created);
	}

	@Test
	public void testUpdateTaskStartPingProcessStatusInProgressToCompleted() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setStatus(TaskStatus.INPROGRESS);
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference()
				.setReferenceElement(organizationProvider.getLocalOrganization().get().getIdElement().toVersionless());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		TaskDao dao = getSpringWebApplicationContext().getBean(TaskDao.class);
		Task created = dao.create(t);

		created.setStatus(TaskStatus.COMPLETED);

		getWebserviceClient().update(created);
	}

	@Test
	public void testCreateForbiddenLocalUserIllegalStatus() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		EnumSet<TaskStatus> illegalCreateStates = EnumSet.of(TaskStatus.RECEIVED, TaskStatus.ACCEPTED,
				TaskStatus.REJECTED, TaskStatus.READY, TaskStatus.CANCELLED, TaskStatus.INPROGRESS, TaskStatus.ONHOLD,
				TaskStatus.FAILED, TaskStatus.COMPLETED, TaskStatus.ENTEREDINERROR, TaskStatus.NULL);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.setStatus(null);
		testCreateExpectForbidden(getWebserviceClient(), t);

		for (TaskStatus illegal : illegalCreateStates)
		{
			t.setStatus(illegal);
			testCreateExpectForbidden(getWebserviceClient(), t);
		}
	}

	@Test
	public void testCreateForbiddenExternalUserIllegalStatus() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		EnumSet<TaskStatus> illegalCreateStates = EnumSet.of(TaskStatus.RECEIVED, TaskStatus.ACCEPTED,
				TaskStatus.REJECTED, TaskStatus.READY, TaskStatus.CANCELLED, TaskStatus.INPROGRESS, TaskStatus.ONHOLD,
				TaskStatus.FAILED, TaskStatus.COMPLETED, TaskStatus.ENTEREDINERROR, TaskStatus.NULL);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setAuthoredOn(new Date());
		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);
		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()));
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.setStatus(null);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		for (TaskStatus illegal : illegalCreateStates)
		{
			t.setStatus(illegal);
			testCreateExpectForbidden(getExternalWebserviceClient(), t);
		}
	}

	private void testCreateExpectForbidden(FhirWebserviceClient client, Task task) throws Exception
	{
		try
		{
			client.create(task);
			fail("WebApplicationException expected");
		}
		catch (WebApplicationException e)
		{
			assertEquals(403, e.getResponse().getStatus());
		}
	}

	@Test
	public void testCreateForbiddenLocalUserNotPartOfRequesterOrganization() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.setRequester(null);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setRequester(new Reference());
		testCreateExpectForbidden(getWebserviceClient(), t);

		Reference requester1 = new Reference().setType("Organization");
		requester1.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester1);
		testCreateExpectForbidden(getWebserviceClient(), t);

		Reference requester2 = new Reference()
				.setReference("http://foo.test/fhir/Organization/" + UUID.randomUUID().toString());
		t.setRequester(requester2);
		testCreateExpectForbidden(getWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenExternalUserNotPartOfRequesterOrganization() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()));
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.setRequester(null);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setRequester(new Reference());
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		Reference requester1 = new Reference()
				.setReferenceElement(organizationProvider.getLocalOrganization().get().getIdElement().toVersionless());
		t.setRequester(requester1);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		Reference requester2 = new Reference()
				.setReference("http://foo.test/fhir/Organization/" + UUID.randomUUID().toString());
		t.setRequester(requester2);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenLocalUserRestrictionRecipientNotValidByLocalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.setRestriction(null);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.getRestriction().addExtension().setUrl("test");
		testCreateExpectForbidden(getWebserviceClient(), t);

		Reference requester0 = new Reference().setReference("Organization/" + UUID.randomUUID().toString());
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester0);
		testCreateExpectForbidden(getWebserviceClient(), t);

		Reference requester1 = new Reference().setType("Organization");
		requester1.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester1);
		testCreateExpectForbidden(getWebserviceClient(), t);

		Reference requester2 = new Reference()
				.setReference("http://foo.test/fhir/Organization/" + UUID.randomUUID().toString());
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester2);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester1).addRecipient(requester2);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()))
				.addRecipient(requester0);
		testCreateExpectForbidden(getWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenLocalUserRestrictionRecipientNotValidByExternalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.setRestriction(null);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.getRestriction().addExtension().setUrl("test");
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		Reference requester0 = new Reference().setReference("Organization/" + UUID.randomUUID().toString());
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester0);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		Reference requester1 = new Reference().setType("Organization");
		requester1.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester1);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		Reference requester2 = new Reference()
				.setReference("http://foo.test/fhir/Organization/" + UUID.randomUUID().toString());
		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester2);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(requester1).addRecipient(requester2);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setRestriction(new TaskRestrictionComponent());
		t.getRestriction().addRecipient(new Reference(organizationProvider.getLocalOrganization().get()))
				.addRecipient(requester0);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenInstantiatesUriNotValidByLocalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		// t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.setInstantiatesUri(null);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInstantiatesUri("not-a-valid-pattern");
		testCreateExpectForbidden(getWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenInstantiatesUriNotValidByExternalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		// t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.setInstantiatesUri(null);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInstantiatesUri("not-a-valid-pattern");
		testCreateExpectForbidden(getExternalWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenInputNotValidByLocalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		// t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
		// .setCode("message-name");
		// t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.setInput(null);
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("system").setCode("code");
		t.getInputFirstRep().setValue(new StringType("value"));
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		ParameterComponent in1 = t.addInput();
		in1.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		in1.setValue(new StringType("startProcessMessage"));
		ParameterComponent in2 = t.addInput();
		in2.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		in2.setValue(new StringType("startProcessMessage"));
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType(""));
		testCreateExpectForbidden(getWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new Coding().setSystem("system").setCode("code"));
		testCreateExpectForbidden(getWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenInputNotValidByExternalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.getRestriction().addRecipient(localOrg);
		// t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
		// .setCode("message-name");
		// t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.setInput(null);
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("system").setCode("code");
		t.getInputFirstRep().setValue(new StringType("value"));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		ParameterComponent in1 = t.addInput();
		in1.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		in1.setValue(new StringType("startProcessMessage"));
		ParameterComponent in2 = t.addInput();
		in2.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		in2.setValue(new StringType("startProcessMessage"));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType(""));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);

		t.setInput(null);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new Coding().setSystem("system").setCode("code"));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenOutputNotValidByLocalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-process");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.setRequester(localOrg);
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("startProcessMessage"));

		t.getOutputFirstRep().getType().getCodingFirstRep().setSystem("system").setCode("code");
		t.getOutputFirstRep().setValue(new StringType("value"));
		testCreateExpectForbidden(getWebserviceClient(), t);
	}

	@Test
	public void testCreateForbiddenOutputNotValidByExternalUser() throws Exception
	{
		OrganizationProvider organizationProvider = getSpringWebApplicationContext()
				.getBean(OrganizationProvider.class);
		assertNotNull(organizationProvider);

		Task t = new Task();
		t.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.1.0");
		t.setIntent(TaskIntent.ORDER);
		t.setStatus(TaskStatus.DRAFT);
		t.setAuthoredOn(new Date());
		Reference requester = new Reference().setType("Organization");
		requester.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("External_Test_Organization");
		t.setRequester(requester);
		Reference localOrg = new Reference(organizationProvider.getLocalOrganization().get());
		t.getRestriction().addRecipient(localOrg);
		t.getInputFirstRep().getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		t.getInputFirstRep().setValue(new StringType("pingMessage"));

		t.getOutputFirstRep().getType().getCodingFirstRep().setSystem("system").setCode("code");
		t.getOutputFirstRep().setValue(new StringType("value"));
		testCreateExpectForbidden(getExternalWebserviceClient(), t);
	}
}
