package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ValidationResult;

public class CodeSystemProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-code-system-0.5.0.xml", "highmed-extension-read-access-organization-0.5.0.xml",
					"highmed-extension-read-access-consortium-role-0.5.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-organization-role-0.6.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-organization-role-0.6.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	private CodeSystem createCodeSystem()
	{
		var cs = new CodeSystem();
		cs.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/code-system");
		cs.setUrl("http://test.com/fhir/test");
		cs.setVersion("0.1.0");
		cs.setName("Test");
		cs.setStatus(PublicationStatus.ACTIVE);
		cs.setContent(CodeSystemContentMode.COMPLETE);
		cs.addConcept().setCode("TEST").setDisplay("Test Display").setDefinition("Test Definition");
		cs.setCaseSensitive(true);

		return cs;
	}

	private void logMessages(ValidationResult result)
	{
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::debug);
	}

	private void logResource(Resource resource)
	{
		logger.trace("{}",
				validationRule.getFhirContext().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource));
	}

	@Test
	public void testCodeSystemWithAllReadAccessValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertTrue(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithLocalReadAccessValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertTrue(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithFooReadAccessNotValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("FOO");

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertEquals(1, result.getMessages().size());
		assertFalse(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithOrganizationReadAccessValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ORGANIZATION")
				.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-organization")
				.setValue(new Identifier().setSystem("http://highmed.org/sid/organization-identifier")
						.setValue("foo.com"));

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertTrue(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithTwoOrganizationsReadAccessValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ORGANIZATION")
				.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-organization")
				.setValue(new Identifier().setSystem("http://highmed.org/sid/organization-identifier")
						.setValue("foo.com"));
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ORGANIZATION")
				.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-organization")
				.setValue(new Identifier().setSystem("http://highmed.org/sid/organization-identifier")
						.setValue("bar.com"));

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertTrue(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithConsortiumMemberReadAccessValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		Extension ex = cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag")
				.setCode("ROLE").addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role");
		ex.addExtension().setUrl("consortium").setValue(new Identifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("consortium.com"));
		ex.addExtension().setUrl("role").setValue(
				new Coding().setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("TTP"));

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertTrue(result.isSuccessful());
	}

	@Test
	public void testCodeSystemWithConsortiumMemberReadAccessNotValid() throws Exception
	{
		CodeSystem cs = createCodeSystem();
		cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("LOCAL");
		Extension ex = cs.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag")
				.setCode("ROLE").addExtension()
				.setUrl("http://highmed.org/fhir/StructureDefinition/extension-read-access-consortium-role");
		ex.addExtension().setUrl("consortium").setValue(new Identifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("consortium.com"));
		ex.addExtension().setUrl("role").setValue(
				new Coding().setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("FOO"));

		logResource(cs);

		ValidationResult result = resourceValidator.validate(cs);

		logMessages(result);

		assertEquals(1, result.getMessages().size());
		assertFalse(result.isSuccessful());
	}
}
