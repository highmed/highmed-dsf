package org.highmed.dsf.fhir.profile;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.UnsignedIntType;
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
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(Arrays.asList(
			"highmed-task-base-0.4.0.xml", "highmed-group.xml", "highmed-extension-group-id.xml",
			"highmed-research-study-feasibility.xml", "highmed-task-request-simple-feasibility.xml",
			"highmed-task-execute-simple-feasibility.xml", "highmed-task-single-medic-result-simple-feasibility.xml",
			"highmed-task-compute-simple-feasibility.xml", "highmed-task-multi-medic-result-simple-feasibility.xml",
			"highmed-task-error-simple-feasibility.xml"),
			Arrays.asList("authorization-role-0.4.0.xml", "bpmn-message-0.4.0.xml", "feasibility.xml"),
			Arrays.asList("authorization-role-0.4.0.xml", "bpmn-message-0.4.0.xml", "feasibility.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskRequestSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskRequestSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskRequestSimpleFeasibilityValidWithOutput() throws Exception
	{
		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		Task task = createValidTaskRequestSimpleFeasibility();

		TaskOutputComponent outParticipatingMedics1 = task.addOutput();
		outParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		outParticipatingMedics1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));
		TaskOutputComponent outMultiMedicResult1 = task.addOutput();
		outMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		outMultiMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));

		TaskOutputComponent outParticipatingMedics2 = task.addOutput();
		outParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		outParticipatingMedics2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));
		TaskOutputComponent outMultiMedicResult2 = task.addOutput();
		outMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		outMultiMedicResult2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("research-study-reference");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");

		return task;
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValidWithBloomFilterConfig() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();
		task.addInput().setValue(new Base64BinaryType("TEST".getBytes(StandardCharsets.UTF_8))).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("bloom-filter-configuration");

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-execute-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/executeSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_2");

		task.addInput().setValue(new StringType("executeSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("research-study-reference");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");

		return task;
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityUnsignedIntResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityReferenceResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(
				"http://highmed.org/fhir/StructureDefinition/highmed-task-single-medic-result-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/computeSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_2");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("resultSingleMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("correlation-key");

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result");
		inSingleMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new UnsignedIntType(10)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result");
		inSingleMedicResult2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result-reference");
		inSingleMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("single-medic-result-reference");
		inSingleMedicResult2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskComputeSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskComputeSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskComputeSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-compute-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/computeSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("computeSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("medic-correlation-key");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("medic-correlation-key");

		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");

		return task;
	}

	@Test
	public void testTaskMultiMedicResultSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskMultiMedicResultSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskMultiMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(
				"http://highmed.org/fhir/StructureDefinition/highmed-task-multi-medic-result-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("resultMultiMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inParticipatingMedics1 = task.addInput();
		inParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		inParticipatingMedics1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));
		ParameterComponent inMultiMedicResult1 = task.addInput();
		inMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		inMultiMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId1));

		ParameterComponent inParticipatingMedics2 = task.addInput();
		inParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("participating-medics");
		inParticipatingMedics2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));
		ParameterComponent inMultiMedicResult2 = task.addInput();
		inMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("multi-medic-result");
		inMultiMedicResult2.addExtension("http://highmed.org/fhir/StructureDefinition/group-id",
				new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskErrorSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskErrorSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskErrorSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-error-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.4.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("errorMultiMedicSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("business-key");

		ParameterComponent error = task.addInput();
		error.setValue(new StringType(
				"An error occurred while calculating the multi medic feasibility result for all defined cohorts"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("error");

		return task;
	}
}
