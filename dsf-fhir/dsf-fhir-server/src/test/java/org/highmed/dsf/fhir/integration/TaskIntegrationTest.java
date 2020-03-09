package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.Test;

public class TaskIntegrationTest extends AbstractIntegrationTest
{
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

			String taskId = new IdType(resultBundleEntries.get(5).getFullUrl()).getIdPart();
			Task task = getWebserviceClient().read(Task.class, taskId);

			Task.ParameterComponent input = task.getInput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.ParameterComponent());

			IdType taskInputResearchStudyId = new IdType(((Reference) input.getValue()).getReference());
			IdType researchStudyId = new IdType(resultBundleEntries.get(4).getFullUrl());

			assertEquals(researchStudyId.getResourceType(), taskInputResearchStudyId.getResourceType());
			assertEquals(researchStudyId.getIdPart(), taskInputResearchStudyId.getIdPart());

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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/1.0.0");
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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/1.0.0");
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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/pong/1.0.0");
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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/1.0.0");
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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/1.0.0");
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
		t.setInstantiatesUri("http://highmed.org/bpe/Process/ping/1.0.0");
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
}
