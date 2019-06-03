package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class SnapshotTest
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotTest.class);

	@Test
	public void testSnapshot() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		StructureDefinitionReader reader = new StructureDefinitionReader(context);
		var validationSupport = new DefaultProfileValidationSupportWithCustomResources(
				Collections.singletonList(
						reader.readXml(Paths.get("src/test/resources/profiles/extension-workflow-researchstudy.xml"))),
				Collections.emptyList(), Collections.emptyList());

		IWorkerContext worker = new HapiWorkerContext(context, validationSupport);
		List<ValidationMessage> messages = new ArrayList<>();

		ProfileUtilities profileUtis = new ProfileUtilities(worker, messages, null);

		String url = "";
		String profileName = "highmed-data-sharing-task";
		StructureDefinition base = worker.fetchTypeDefinition(Task.class.getAnnotation(ResourceDef.class).name())
				.copy();
		StructureDefinition derived = reader.readXml(Paths.get("src/test/resources/profiles/highmed-task-0.5.0.xml"));

		profileUtis.generateSnapshot(base, derived, url, profileName);

		logger.info("Snapshot: " + context.newXmlParser().encodeResourceToString(derived));

		messages.forEach(m -> logger.error("Issue while generating snapshot: {} - {} - {}", m.getDisplay(), m.getLine(),
				m.getMessage()));
	}

	@Test
	public void testSnapshotGenerator() throws Exception
	{
		FhirContext context = FhirContext.forR4();
		StructureDefinitionReader reader = new StructureDefinitionReader(context);

		StructureDefinition structureDefinition = reader
				.readXml(Paths.get("src/test/resources/profiles/extension-workflow-researchstudy.xml"));
		SnapshotGenerator generator = new SnapshotGeneratorImpl(context,
				new DefaultProfileValidationSupportWithCustomResources(Collections.singletonList(structureDefinition),
						Collections.emptyList(), Collections.emptyList()));

		SnapshotWithValidationMessages snapshot = generator
				.generateSnapshot(reader.readXml(Paths.get("src/test/resources/profiles/highmed-task-0.5.0.xml")));

		assertNotNull(snapshot);
		assertNotNull(snapshot.getSnapshot());
		assertNotNull(snapshot.getMessages());
		assertTrue(snapshot.getMessages().isEmpty());

		assertTrue(snapshot.getSnapshot().getSnapshot().getElement().stream().map(ElementDefinition::getId)
				.anyMatch(id -> "Task.extension:researchStudy".equals(id)));

		snapshot.getSnapshot().getSnapshot().getElement()
				.forEach(e -> logger.debug("snapshot.element.path#id: {}", e.getId()));
	}
}
