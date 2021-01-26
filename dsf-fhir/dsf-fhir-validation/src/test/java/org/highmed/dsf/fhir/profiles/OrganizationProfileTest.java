package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class OrganizationProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-organization-0.4.0.xml", "highmed-extension-certificate-thumbprint-0.4.0.xml",
					"highmed-endpoint-0.4.0.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-organization-type-0.4.0.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-organization-type-0.4.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testOrganizationProfileValid() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test");
		org.setActive(true);
		org.getTypeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-type")
				.setCode("TTP");
		org.addEndpoint().setReference("Endpoint/" + UUID.randomUUID().toString());
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid1() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test");
		org.setActive(true);
		org.getTypeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-type")
				.setCode("NON_EXISTING_CODE");
		org.addEndpoint().setReference("Endpoint/" + UUID.randomUUID().toString());
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(2, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid2() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test");
		org.setActive(true);
		org.getTypeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-type");
		org.addEndpoint().setReference("Endpoint/" + UUID.randomUUID().toString());
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(2, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid3() throws Exception
	{
		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test");
		org.setActive(true);
		org.addEndpoint().setReference("Endpoint/" + UUID.randomUUID().toString());
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(2, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}
}
