package org.highmed.fhir.hapi;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.UUID;

import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.SnapshotGenerator;
import org.highmed.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.fhir.service.StructureDefinitionReader;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;

public class HiGHmedTaskValidationTest
{
	private static final Logger logger = LoggerFactory.getLogger(HiGHmedTaskValidationTest.class);

	@Test
	public void testTaskValid() throws Exception
	{
		FhirContext context = FhirContext.forR4();
		StructureDefinition highmedTaskDiff = new StructureDefinitionReader(context)
				.readXml(Paths.get("src/test/resources/task-highmed-0.0.1.xml"));

		SnapshotGenerator generator = new SnapshotGenerator(context, highmedTaskDiff);
		SnapshotWithValidationMessages highmedTask = generator.generateSnapshot("Task", "", highmedTaskDiff);

		assertNotNull(highmedTask);
		assertNotNull(highmedTask.getMessages());
		assertTrue(highmedTask.getMessages().isEmpty());

		StructureDefinition snapshot = highmedTask.getSnapshot();
		snapshot.setUrl(highmedTaskDiff.getUrl());
		
		logger.debug("Snapshot: " + context.newXmlParser().setPrettyPrint(true).encodeResourceToString(snapshot));

		logger.info("Snapshot URL: {}", snapshot.getUrl());

		Task task = new Task();
		task.getMeta().addProfile(snapshot.getUrl());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		Extension ext = task.addExtension();
		ext.setUrl(highmedTaskDiff.getUrl());
		Reference researchStudyReference = new Reference();
		researchStudyReference.setReference("ResearchStudy/" + UUID.randomUUID().toString());
		ext.setValue(researchStudyReference);

		logger.debug("Task: " + context.newXmlParser().setPrettyPrint(true).encodeResourceToString(task));

		ResourceValidator validator = new ResourceValidator(context, snapshot);
		ValidationResult validationResult = validator.validate(task);

		validationResult.getMessages().forEach(m -> logger.info("Validation Issue: {} - {} - {}", m.getSeverity(),
				m.getLocationString(), m.getMessage()));

		assertTrue(validationResult.isSuccessful());
	}
}
