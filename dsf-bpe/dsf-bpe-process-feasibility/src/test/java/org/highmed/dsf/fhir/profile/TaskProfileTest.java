package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.COMPUTE_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.COMPUTE_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.ERROR_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.ERROR_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXECUTE_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXECUTE_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXTENSION_GROUP_ID_URI;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.MULTI_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.SINGLE_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE;
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
import org.hl7.fhir.r4.model.ResourceType;
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
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-task-base-0.4.0.xml", "highmed-group.xml", "highmed-extension-group-id.xml",
					"highmed-research-study-feasibility.xml", "highmed-task-request-simple-feasibility.xml",
					"highmed-task-execute-simple-feasibility.xml",
					"highmed-task-single-medic-result-simple-feasibility.xml",
					"highmed-task-compute-simple-feasibility.xml",
					"highmed-task-multi-medic-result-simple-feasibility.xml",
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

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskRequestSimpleFeasibilityValidWithOutput() throws Exception
	{
		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		Task task = createValidTaskRequestSimpleFeasibility();

		TaskOutputComponent outParticipatingMedics1 = task.addOutput();
		outParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		outParticipatingMedics1.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId1));
		TaskOutputComponent outMultiMedicResult1 = task.addOutput();
		outMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY).setCode("multi-medic-result");
		outMultiMedicResult1
				.addExtension("http://highmed.org/fhir/StructureDefinition/group-id", new Reference(groupId1));

		TaskOutputComponent outParticipatingMedics2 = task.addOutput();
		outParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		outParticipatingMedics2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));
		TaskOutputComponent outMultiMedicResult2 = task.addOutput();
		outMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		outMultiMedicResult2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(REQUEST_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(REQUEST_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskExecuteSimpleFeasibilityValidWithBloomFilterConfig() throws Exception
	{
		Task task = createValidTaskExecuteSimpleFeasibility();
		task.addInput().setValue(new Base64BinaryType("TEST".getBytes(StandardCharsets.UTF_8))).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(EXECUTE_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 2");

		task.addInput().setValue(new StringType(EXECUTE_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityUnsignedIntResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskSingleMedicResultSimpleFeasibilityReferenceResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(SINGLE_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 2");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("TTP");

		task.addInput().setValue(new StringType(SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityUnsignedIntResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT);
		inSingleMedicResult1.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new UnsignedIntType(10)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT);
		inSingleMedicResult2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));

		return task;
	}

	private Task createValidTaskSingleMedicResultSimpleFeasibilityReferenceResult()
	{
		Task task = createValidTaskSingleMedicResultSimpleFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE);
		inSingleMedicResult1.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE);
		inSingleMedicResult2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskComputeSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskComputeSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskComputeSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(COMPUTE_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("TTP");

		task.addInput().setValue(new StringType(COMPUTE_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY);

		task.addInput().setValue(new BooleanType(false)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);

		return task;
	}

	@Test
	public void testTaskMultiMedicResultSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskMultiMedicResultSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskMultiMedicResultSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(MULTI_MEDIC_RESULT_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inParticipatingMedics1 = task.addInput();
		inParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		inParticipatingMedics1.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId1));
		ParameterComponent inMultiMedicResult1 = task.addInput();
		inMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		inMultiMedicResult1.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId1));

		ParameterComponent inParticipatingMedics2 = task.addInput();
		inParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		inParticipatingMedics2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));
		ParameterComponent inMultiMedicResult2 = task.addInput();
		inMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		inMultiMedicResult2.addExtension(EXTENSION_GROUP_ID_URI, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskErrorSimpleFeasibilityValid() throws Exception
	{
		Task task = createValidTaskErrorSimpleFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskErrorSimpleFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(ERROR_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(ERROR_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		ParameterComponent error = task.addInput();
		error.setValue(new StringType(
				"An error occurred while calculating the multi medic feasibility result for all defined cohorts"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR);

		return task;
	}
}
