package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME;
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
			Arrays.asList("highmed-task-base-0.4.0.xml", "highmed-group-0.4.0.xml",
					"highmed-extension-group-id-0.4.0.xml", "highmed-research-study-0.4.0.xml",
					"highmed-task-request-feasibility.xml", "highmed-task-execute-feasibility.xml",
					"highmed-task-single-medic-result-feasibility.xml", "highmed-task-compute-feasibility.xml",
					"highmed-task-multi-medic-result-feasibility.xml", "highmed-task-error-feasibility.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-feasibility.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-feasibility.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskRequestFeasibilityValid() throws Exception
	{
		Task task = createValidTaskRequestFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskRequestFeasibilityValidWithOutput() throws Exception
	{
		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		Task task = createValidTaskRequestFeasibility();

		TaskOutputComponent outParticipatingMedics1 = task.addOutput();
		outParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		outParticipatingMedics1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));
		TaskOutputComponent outMultiMedicResult1 = task.addOutput();
		outMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY).setCode("multi-medic-result");
		outMultiMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/extension-group-id",
				new Reference(groupId1));

		TaskOutputComponent outParticipatingMedics2 = task.addOutput();
		outParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		outParticipatingMedics2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));
		TaskOutputComponent outMultiMedicResult2 = task.addOutput();
		outMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		outMultiMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
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
	public void testTaskExecuteFeasibilityValid() throws Exception
	{
		Task task = createValidTaskExecuteFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskExecuteFeasibilityValidWithBloomFilterConfig() throws Exception
	{
		Task task = createValidTaskExecuteFeasibility();
		task.addInput().setValue(new Base64BinaryType("TEST".getBytes(StandardCharsets.UTF_8))).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 2");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
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
	public void testTaskSingleMedicResultFeasibilityUnsignedIntResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultFeasibilityUnsignedIntResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskSingleMedicResultFeasibilityReferenceResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultFeasibilityReferenceResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskSingleMedicResultFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 2");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN)
				.setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		return task;
	}

	private Task createValidTaskSingleMedicResultFeasibilityUnsignedIntResult()
	{
		Task task = createValidTaskSingleMedicResultFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT);
		inSingleMedicResult1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new UnsignedIntType(10)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT);
		inSingleMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		return task;
	}

	private Task createValidTaskSingleMedicResultFeasibilityReferenceResult()
	{
		Task task = createValidTaskSingleMedicResultFeasibility();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE);
		inSingleMedicResult1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE);
		inSingleMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskComputeFeasibilityValid() throws Exception
	{
		Task task = createValidTaskComputeFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskComputeFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_COMPUTE_FEASIBILITY_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
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
	public void testTaskMultiMedicResultFeasibilityValid() throws Exception
	{
		Task task = createValidTaskMultiMedicResultFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskMultiMedicResultFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_FEASIBILITY_MESSAGE_NAME))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN)
				.setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inParticipatingMedics1 = task.addInput();
		inParticipatingMedics1.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		inParticipatingMedics1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));
		ParameterComponent inMultiMedicResult1 = task.addInput();
		inMultiMedicResult1.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		inMultiMedicResult1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));

		ParameterComponent inParticipatingMedics2 = task.addInput();
		inParticipatingMedics2.setValue(new UnsignedIntType(5)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		inParticipatingMedics2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));
		ParameterComponent inMultiMedicResult2 = task.addInput();
		inMultiMedicResult2.setValue(new UnsignedIntType(25)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		inMultiMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskErrorFeasibilityValid() throws Exception
	{
		Task task = createValidTaskErrorFeasibility();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskErrorFeasibility()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_ERROR_FEASIBILITY_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		ParameterComponent error = task.addInput();
		error.setValue(new StringType(
				"An error occurred while calculating the multi medic feasibility result for all defined cohorts"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR);

		return task;
	}
}
