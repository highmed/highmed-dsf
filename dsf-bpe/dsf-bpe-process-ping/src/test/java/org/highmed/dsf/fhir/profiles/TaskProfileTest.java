package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
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
			Arrays.asList("highmed-task-base-0.3.0.xml", "highmed-task-start-ping-process-0.3.0.xml",
					"highmed-task-ping-0.3.0.xml", "highmed-task-pong-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "bpmn-message-0.3.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskStartPingProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartPingProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid1() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid2() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setIntent(TaskIntent.FILLERORDER);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid3() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setAuthoredOn(null);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartPingProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-start-ping-process");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("startPingProcessMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		return task;
	}

	@Test
	public void testTaskPingValid() throws Exception
	{
		Task task = createValidTaskPing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPing()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-ping");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/pong/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("pingMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}

	@Test
	public void testTaskPongValid() throws Exception
	{
		Task task = createValidTaskPong();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPong()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-pong");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("pongMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}
}
