package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION;
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
import org.hl7.fhir.r4.model.ResourceType;
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
			Arrays.asList("highmed-task-base-0.4.0.xml", "highmed-task-request-update-resources.xml",
					"highmed-task-execute-update-resources.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-update-resources.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-update-resources.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testGenerateSnapshotNotWorkingWithoutBaseSnapshot() throws Exception
	{
		var reader = new StructureDefinitionReader(validationRule.getFhirContext());

		StructureDefinition base = reader.readXml("/fhir/StructureDefinition/highmed-task-base-0.4.0.xml");
		StructureDefinition differential = reader
				.readXml("/fhir/StructureDefinition/highmed-task-execute-update-resources.xml");

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

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE);
		task.addInput().setValue(new StringType("http://highmed.org/fhir/NamingSystem/organization-identifier|"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER);

		return task;
	}

	@Test
	public void testTaskExecuteUpdateResourcesValid() throws Exception
	{
		Task task = createValidTaskExecuteUpdateResources();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteUpdateResources()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_EXECUTE_UPDATE_RESOURCES_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new Reference("Bundle/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE);

		return task;
	}
}
