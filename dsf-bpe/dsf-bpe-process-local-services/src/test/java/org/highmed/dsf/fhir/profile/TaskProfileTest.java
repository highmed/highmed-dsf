package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
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
					"highmed-extension-query.xml", "highmed-task-local-services-integration.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-feasibility.xml", "highmed-query-type.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-bpmn-message-0.4.0.xml",
					"highmed-feasibility.xml", "highmed-query-type.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskLocalServiceIntegrationValid() throws Exception
	{
		Task task = createValidTaskLocalServiceIntegration();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	private Task createValidTaskLocalServiceIntegration()
	{
		Task task = new Task();

		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_LOCAL_SERVICES);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_LOCAL_SERVICES_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new StringType("SELECT COUNT(e) FROM EHR e;")).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_QUERY_TYPE).setCode(CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);

		byte[] bloomFilterConfig = Base64.getDecoder()
				.decode("CIw/x19d3Oj+GLOKgYAX5KrFAl11q6qMi0qkDiyUOCvMXuF2KffVvSnjUjkTvqh4z8Xs+MuQdK6FqTedM5FY9t4qm+k92A+P");

		task.addInput().setValue(new Base64BinaryType(bloomFilterConfig)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);

		return task;
	}
}
