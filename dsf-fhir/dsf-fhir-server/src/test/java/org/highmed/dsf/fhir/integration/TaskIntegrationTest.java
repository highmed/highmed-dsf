package org.highmed.dsf.fhir.integration;

import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class TaskIntegrationTest extends AbstractIntegrationTest
{
	private static WebserviceClient client;
	private static Bundle resultBundle;
	private static List<Bundle.BundleEntryComponent> resultBundleEntries;
	private static String researchStudyId;
	private static Task task;

	@BeforeClass
	public static void init()
	{
		Bundle bundle = readBundle(Paths.get("src/test/resources/integration/task-bundle.json"), newJsonParser());

		client = getWebserviceClient();

		resultBundle = client.postBundle(bundle);
		resultBundleEntries = resultBundle.getEntry();

		researchStudyId = resultBundleEntries.get(0).getFullUrl().substring(BASE_URL.length());
		task = client.read(Task.class, resultBundleEntries.get(1).getFullUrl().substring(BASE_URL.concat("Task/").length()));
	}

	@Test
	public void testInputTransactionReferenceResolver() throws Exception
	{
		Task.ParameterComponent component = task.getInput().stream()
				.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference")).findFirst()
				.orElse(new Task.ParameterComponent());
		String taskInputResearchStudyId = ((Reference) component.getValue()).getReference();

		assertEquals(researchStudyId, taskInputResearchStudyId);
	}

	@Test
	public void testOutputTransactionReferenceResolver() throws Exception
	{
		Task.TaskOutputComponent component = task.getOutput().stream()
				.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference")).findFirst()
				.orElse(new Task.TaskOutputComponent());
		String taskOutputResearchStudyId = ((Reference) component.getValue()).getReference();

		assertEquals(researchStudyId, taskOutputResearchStudyId);
	}
}
