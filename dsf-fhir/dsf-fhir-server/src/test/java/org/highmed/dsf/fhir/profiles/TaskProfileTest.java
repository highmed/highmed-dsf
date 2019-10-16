package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.ResourceValidatorImpl;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.highmed.dsf.fhir.service.ValueSetExpander;
import org.highmed.dsf.fhir.service.ValueSetExpanderImpl;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileTest.class);

	private FhirContext context;
	private DefaultProfileValidationSupportWithCustomResources validationSupport;

	private ResourceValidator resourceValidator;
	private SnapshotGenerator snapshotGenerator;
	private ValueSetExpander valueSetExpander;

	@Before
	public void before() throws Exception
	{
		context = FhirContext.forR4();
		validationSupport = new DefaultProfileValidationSupportWithCustomResources();

		resourceValidator = new ResourceValidatorImpl(context, validationSupport);
		snapshotGenerator = new SnapshotGeneratorImpl(context, validationSupport);
		valueSetExpander = new ValueSetExpanderImpl(context, validationSupport);
	}

	private void readProfilesAndGenerateSnapshots()
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);
		List<StructureDefinition> diffs = reader.readXml(
				Paths.get("src/test/resources/profiles", "highmed-extension-certificate-thumbprint-0.5.0.xml"),
				Paths.get("src/test/resources/profiles", "highmed-organization-0.5.0.xml"),
				Paths.get("src/test/resources/profiles", "highmed-endpoint-0.5.0.xml"),
				Paths.get("src/test/resources/profiles", "highmed-task-0.5.0.xml"));

		for (StructureDefinition diff : diffs)
		{
			SnapshotWithValidationMessages snapshotWithValidationMessages = snapshotGenerator.generateSnapshot(diff);

			if (snapshotWithValidationMessages.getSnapshot() != null)
				validationSupport.addOrReplaceStructureDefinition(snapshotWithValidationMessages.getSnapshot());
		}
	}

	private void readCodeSystems()
	{
		CodeSystem bpmnMessage = new CodeSystem();
		bpmnMessage.setStatus(PublicationStatus.ACTIVE);
		bpmnMessage.setContent(CodeSystemContentMode.COMPLETE);
		bpmnMessage.setUrl("http://highmed.org/fhir/CodeSystem/bpmn-message");
		bpmnMessage.setVersion("0.5.0");
		bpmnMessage.addConcept().setCode("message-name");
		bpmnMessage.addConcept().setCode("business-key");
		bpmnMessage.addConcept().setCode("correlation-key");

		validationSupport.addOrReplaceCodeSystem(bpmnMessage);

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/bpmn-message");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message");

		validationSupport.addOrReplaceValueSet(valueSetExpander.expand(valueSet).getValueset());
	}

	@Test
	public void testTaskProfileValid() throws Exception
	{
		readProfilesAndGenerateSnapshots();
		readCodeSystems();

		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/executeUpdateResources/1.0.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setReference("Organization/" + UUID.randomUUID().toString());
		task.getRestriction().getRecipientFirstRep().setReference("Organization/" + UUID.randomUUID().toString());
		var messageName = task.addInput();
		messageName.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("message-name");
		messageName.setValue(new StringType("foo"));
		var businessKey = task.addInput();
		businessKey.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("business-key");
		businessKey.setValue(new StringType(UUID.randomUUID().toString()));
		var correlationKey = task.addInput();
		correlationKey.getType().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message")
				.setCode("correlation-key");
		correlationKey.setValue(new StringType(UUID.randomUUID().toString()));
		task.addExtension().setUrl("http://hl7.org/fhir/StructureDefinition/workflow-researchStudy")
				.setValue(new Reference("https://medic1/fhir/ResearchStudy/" + UUID.randomUUID().toString()));

		logger.debug("Task: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(task));

		ValidationResult result = resourceValidator.validate(task);

		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
	}
}
