package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
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
	public void testInputTransactionReferenceResolver() throws Exception
	{
		WebsocketClient websocketClient = getWebsocketClient();
		assertNotNull(websocketClient);

		BlockingDeque<DomainResource> events = new LinkedBlockingDeque<>();
		websocketClient.setDomainResourceHandler(events::add, AbstractIntegrationTest::newJsonParser);
		websocketClient.connect();

		try
		{
			List<Bundle.BundleEntryComponent> resultBundleEntries = createTaskBundle();

			String taskId = new IdType(resultBundleEntries.get(1).getFullUrl()).getIdPart();
			Task task = getWebserviceClient().read(Task.class, taskId);

			Task.ParameterComponent input = task.getInput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.ParameterComponent());

			IdType taskInputResearchStudyId = new IdType(((Reference) input.getValue()).getReference());
			IdType researchStudyId = new IdType(resultBundleEntries.get(0).getFullUrl());

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
	public void testOutputTransactionReferenceResolver() throws Exception
	{
		WebsocketClient websocketClient = getWebsocketClient();
		assertNotNull(websocketClient);

		BlockingDeque<DomainResource> events = new LinkedBlockingDeque<>();
		websocketClient.setDomainResourceHandler(events::add, AbstractIntegrationTest::newJsonParser);
		websocketClient.connect();

		try
		{
			List<Bundle.BundleEntryComponent> resultBundleEntries = createTaskBundle();

			String taskId = new IdType(resultBundleEntries.get(1).getFullUrl()).getIdPart();
			Task task = getWebserviceClient().read(Task.class, taskId);

			Task.TaskOutputComponent output = task.getOutput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.TaskOutputComponent());

			IdType taskOutputResearchStudyId = new IdType(((Reference) output.getValue()).getReference());
			IdType researchStudyId = new IdType(resultBundleEntries.get(0).getFullUrl());

			assertEquals(researchStudyId.getResourceType(), taskOutputResearchStudyId.getResourceType());
			assertEquals(researchStudyId.getIdPart(), taskOutputResearchStudyId.getIdPart());

			DomainResource event = events.pollFirst(5, TimeUnit.SECONDS);
			assertNotNull(event);
			assertTrue(event instanceof Task);

			Task taskViaWebsocket = (Task) event;
			Task.TaskOutputComponent outputViaWebsocket = taskViaWebsocket.getOutput().stream()
					.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference"))
					.findFirst().orElse(new Task.TaskOutputComponent());

			IdType taskOutputResearchStudyIdViaWebsocket = new IdType(
					((Reference) outputViaWebsocket.getValue()).getReference());
			assertEquals(researchStudyId.getResourceType(), taskOutputResearchStudyIdViaWebsocket.getResourceType());
			assertEquals(researchStudyId.getIdPart(), taskOutputResearchStudyIdViaWebsocket.getIdPart());
		}
		finally
		{
			if (websocketClient != null)
				websocketClient.disconnect();
		}
	}
}
