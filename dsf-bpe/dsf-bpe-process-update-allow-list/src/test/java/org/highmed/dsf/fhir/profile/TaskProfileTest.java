package org.highmed.dsf.fhir.profile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-task-base-0.3.0.xml", "highmed-task-update-allow-list-0.3.0.xml",
					"highmed-task-download-allow-list-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-allow-list-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml", "update-allow-list-0.3.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskUpdateAllowListValid() throws Exception
	{
		Task task = createValidTaskUpdateAllowList();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskUpdateAllowlistValidWithOutput() throws Exception
	{
		Task task = createValidTaskUpdateAllowList();
		task.addOutput().setValue(new Reference(new IdType("Bundle", UUID.randomUUID().toString(), "1"))).getType()
				.addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-allow-list")
				.setCode("highmed_allow_list");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskUpdateAllowList()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/updateAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("updateAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskRequestUpdateResourcesAllowListValid() throws Exception
	{
		Task task = createValidTaskRequestUpdateAllowListResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestUpdateAllowListResources()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-update-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/updateAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("updateAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskDownloadAllowListValid() throws Exception
	{
		Task task = createValidTaskDownloadAllowList();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskDownloadAllowList()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-download-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/downloadAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("downloadAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput()
				.setValue(
						new Reference(new IdType("https://foo.bar/fhir", "Bundle", UUID.randomUUID().toString(), "1")))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-allow-list")
				.setCode("highmed_allow_list");

		return task;
	}
}
