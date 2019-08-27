package org.highmed.dsf.fhir.integration;

import static junit.framework.TestCase.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
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
		List<Bundle.BundleEntryComponent> resultBundleEntries = createTaskBundle();

		String taskId = new IdType(resultBundleEntries.get(1).getFullUrl()).getIdPart();
		Task task = getWebserviceClient().read(Task.class, taskId);

		Task.ParameterComponent component = task.getInput().stream()
				.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference")).findFirst()
				.orElse(new Task.ParameterComponent());

		IdType taskInputResearchStudyId = new IdType(((Reference) component.getValue()).getReference());
		IdType researchStudyId = new IdType(resultBundleEntries.get(0).getFullUrl());

		assertEquals(researchStudyId.getResourceType(), taskInputResearchStudyId.getResourceType());
		assertEquals(researchStudyId.getIdPart(), taskInputResearchStudyId.getIdPart());
	}

	@Test
	public void testOutputTransactionReferenceResolver() throws Exception
	{
		List<Bundle.BundleEntryComponent> resultBundleEntries = createTaskBundle();

		String taskId = new IdType(resultBundleEntries.get(1).getFullUrl()).getIdPart();
		Task task = getWebserviceClient().read(Task.class, taskId);

		Task.TaskOutputComponent component = task.getOutput().stream()
				.filter(c -> c.getType().getCoding().get(0).getCode().equals("research-study-reference")).findFirst()
				.orElse(new Task.TaskOutputComponent());

		IdType taskOutputResearchStudyId = new IdType(((Reference) component.getValue()).getReference());
		IdType researchStudyId = new IdType(resultBundleEntries.get(0).getFullUrl());

		assertEquals(researchStudyId.getResourceType(), taskOutputResearchStudyId.getResourceType());
		assertEquals(researchStudyId.getIdPart(), taskOutputResearchStudyId.getIdPart());
	}
}
