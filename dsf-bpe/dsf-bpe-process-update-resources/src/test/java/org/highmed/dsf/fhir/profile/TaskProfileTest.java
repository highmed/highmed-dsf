package org.highmed.dsf.fhir.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.validation.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.validation.StructureDefinitionReader;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.highmed.dsf.fhir.validation.ValidationSupportWithCustomResources;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-task-base-0.3.0.xml", "highmed-task-request-update-resources-0.3.0.xml",
					"highmed-task-execute-update-resources-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-resources-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-resources-0.3.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testGenerateSnapshotNotWorkingWithoutBaseSnapshot() throws Exception
	{
		var reader = new StructureDefinitionReader(validationRule.getFhirContext());

		StructureDefinition base = reader.readXml("/fhir/StructureDefinition/highmed-task-base-0.3.0.xml");
		StructureDefinition differential = reader
				.readXml("/fhir/StructureDefinition/highmed-task-execute-update-resources-0.3.0.xml");

		var validationSupport = new ValidationSupportChain(
				new InMemoryTerminologyServerValidationSupport(validationRule.getFhirContext()),
				new ValidationSupportWithCustomResources(validationRule.getFhirContext(), Arrays.asList(base),
						Collections.emptyList(), Collections.emptyList()),
				new DefaultProfileValidationSupport(validationRule.getFhirContext()));
		var snapshotGenerator = new SnapshotGeneratorImpl(validationRule.getFhirContext(), validationSupport);

		SnapshotWithValidationMessages messages = snapshotGenerator.generateSnapshot(differential);
		assertFalse(messages.getMessages().isEmpty());
	}

	@Test
	public void testTaskRequestUpdateResourcesValid() throws Exception
	{
		Task task = createValidTaskRequestUpdateResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-update-resources");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestUpdateResources/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestUpdateResourcesMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/update-resources").setCode("bundle-reference");
		task.addInput().setValue(new StringType("http://highmed.org/fhir/NamingSystem/organization-identifier|"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-resources")
				.setCode("organization-identifier-search-parameter");

		return task;
	}

	@Test
	public void testTaskExecuteUpdateResourcesValid() throws Exception
	{
		Task task = createValidTaskExecuteUpdateResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-execute-update-resources");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/executeUpdateResources/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("executeUpdateResourcesMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/update-resources").setCode("bundle-reference");

		return task;
	}
}
