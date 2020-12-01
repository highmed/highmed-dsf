package org.highmed.dsf.fhir.profile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
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
			Arrays.asList("authorization-role-0.4.0.xml", "bpmn-message-0.4.0.xml", "feasibility.xml",
					"query-type.xml"),
			Arrays.asList("authorization-role-0.4.0.xml", "bpmn-message-0.4.0.xml", "feasibility.xml",
					"query-type.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskLocalServiceIntegrationValid() throws Exception
	{
		Task task = createValidTaskLocalServiceIntegration();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskLocalServiceIntegration()
	{
		Task task = new Task();

		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-local-services-integration");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/localServicesIntegration/0.4.0");
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("localServicesIntegrationMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		task.addInput().setValue(new StringType("SELECT COUNT(e) FROM EHR e;")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/query-type").setCode("application/x-aql-query");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");

		byte[] bloomFilterConfig = Base64.getDecoder().decode(
				"CIw/x19d3Oj+GLOKgYAX5KrFAl11q6qMi0qkDiyUOCvMXuF2KffVvSnjUjkTvqh4z8Xs+MuQdK6FqTedM5FY9t4qm+k92A+P");

		task.addInput().setValue(new Base64BinaryType(bloomFilterConfig)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("bloom-filter-configuration");

		return task;
	}
}
