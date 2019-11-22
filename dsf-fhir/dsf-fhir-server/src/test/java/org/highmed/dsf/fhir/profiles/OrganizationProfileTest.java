package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.ResourceValidatorImpl;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.highmed.dsf.fhir.service.ValueSetExpander;
import org.highmed.dsf.fhir.service.ValueSetExpanderImpl;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class OrganizationProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileTest.class);

	private FhirContext context;
	private DefaultProfileValidationSupportWithCustomResources validationSupport;

	private ResourceValidator resourceValidator;
	private SnapshotGenerator snapshotGenerator;
	private ValueSetExpander valueSetExpander;

	@Before
	public void before() throws Exception
	{
		context = FhirContext.forR4();
		validationSupport = new DefaultProfileValidationSupportWithCustomResources();

		resourceValidator = new ResourceValidatorImpl(context, validationSupport);
		snapshotGenerator = new SnapshotGeneratorImpl(context, validationSupport);
		valueSetExpander = new ValueSetExpanderImpl(context, validationSupport);
	}

	@Test
	public void testValueSetExpander() throws Exception
	{
		readCodeSystems();

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/highmed-organization");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization");

		ValueSetExpansionOutcome expanded = valueSetExpander.expand(valueSet);

		assertNotNull(expanded);

		assertNull(expanded.getError());
		assertNull(expanded.getErrorClass());
		assertEquals(1, expanded.getValueset().getExpansion().getTotal());
	}

	@Test
	public void testOrganizationProfileNotValid1() throws Exception
	{
		readProfilesAndGenerateSnapshots();

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(4,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid2() throws Exception
	{
		readProfilesAndGenerateSnapshots();

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://foo.bar/baz").setValue("Test");

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(3,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid3() throws Exception
	{
		readProfilesAndGenerateSnapshots();

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization").setValue("Test");

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(2,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
	}

	@Test
	public void testOrganizationProfileNotValid4() throws Exception
	{
		readProfilesAndGenerateSnapshots();
		readCodeSystems();

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/highmed-organization");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization");

		ValueSetExpansionOutcome expanded = valueSetExpander.expand(valueSet);

		assertNotNull(expanded);

		validationSupport.addOrReplaceValueSet(expanded.getValueset());

		assertNull(expanded.getError());
		assertNull(expanded.getErrorClass());
		assertEquals(1, expanded.getValueset().getExpansion().getTotal());

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization")
				.setValue("NonExisting");

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(3,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.WARNING.equals(m.getSeverity())
				&& "ValueSet http://highmed.org/fhir/ValueSet/highmed-organization not found".equals(m.getMessage()))
				.count());
	}

	@Test
	public void testOrganizationProfileNotValid5() throws Exception
	{
		readProfilesAndGenerateSnapshots();
		readCodeSystems();

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/highmed-organization");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization");

		ValueSetExpansionOutcome expanded = valueSetExpander.expand(valueSet);

		assertNotNull(expanded);

		validationSupport.addOrReplaceValueSet(expanded.getValueset());

		assertNull(expanded.getError());
		assertNull(expanded.getErrorClass());
		assertEquals(1, expanded.getValueset().getExpansion().getTotal());

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization").setValue("Test");

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(2,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.WARNING.equals(m.getSeverity())
				&& "ValueSet http://highmed.org/fhir/ValueSet/highmed-organization not found".equals(m.getMessage()))
				.count());
	}

	@Test
	public void testOrganizationProfileNotValid6() throws Exception
	{
		readProfilesAndGenerateSnapshots();
		readCodeSystems();

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/highmed-organization");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization");

		ValueSetExpansionOutcome expanded = valueSetExpander.expand(valueSet);

		assertNotNull(expanded);

		validationSupport.addOrReplaceValueSet(expanded.getValueset());

		assertNull(expanded.getError());
		assertNull(expanded.getErrorClass());
		assertEquals(1, expanded.getValueset().getExpansion().getTotal());

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization").setValue("Test");
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(1,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.WARNING.equals(m.getSeverity())
				&& "ValueSet http://highmed.org/fhir/ValueSet/highmed-organization not found".equals(m.getMessage()))
				.count());
	}

	@Test
	public void testOrganizationProfileValid() throws Exception
	{
		readProfilesAndGenerateSnapshots();
		readCodeSystems();

		ValueSet valueSet = new ValueSet();
		valueSet.setUrl("http://highmed.org/fhir/ValueSet/highmed-organization");
		valueSet.setStatus(PublicationStatus.ACTIVE);
		valueSet.getCompose().addInclude().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization");

		ValueSetExpansionOutcome expanded = valueSetExpander.expand(valueSet);

		assertNotNull(expanded);

		validationSupport.addOrReplaceValueSet(expanded.getValueset());

		assertNull(expanded.getError());
		assertNull(expanded.getErrorClass());
		assertEquals(1, expanded.getValueset().getExpansion().getTotal());

		// Endpoint ept = new Endpoint();
		// ept.setIdElement(new IdType("Endpoint", UUID.randomUUID().toString()));
		// ept.getMeta().setVersionId("1");
		// ept.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-endpoint");

		Organization org = new Organization();
		org.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-organization");
		org.setName("Test Organization");
		org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/highmed-organization").setValue("Test");
		org.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint")
				.setValue(new StringType("A2BF39FF2A7E3D218A32AADE3B2AAA1F"));
		// org.addEndpoint().setReference(ept.getIdElement().getValue()).setResource(ept);
		org.addEndpoint().setReference("Endpoint/" + UUID.randomUUID().toString());

		ValidationResult result = resourceValidator.validate(org);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0,
				result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())).count());
	}

	private void readProfilesAndGenerateSnapshots()
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);
		List<StructureDefinition> diffs = reader.readXml(
				Paths.get("src/test/resources/profiles", "highmed-extension-certificate-thumbprint-0.5.0.xml"),
				Paths.get("src/test/resources/profiles", "highmed-organization-0.5.0.xml"),
				Paths.get("src/test/resources/profiles", "highmed-endpoint-0.5.0.xml"));

		for (StructureDefinition diff : diffs)
		{
			SnapshotWithValidationMessages snapshotWithValidationMessages = snapshotGenerator.generateSnapshot(diff);

			if (snapshotWithValidationMessages.getSnapshot() != null)
				validationSupport.addOrReplaceStructureDefinition(snapshotWithValidationMessages.getSnapshot());
		}
	}

	private void readCodeSystems()
	{
		CodeSystem highmedOrganization = new CodeSystem();
		highmedOrganization.setStatus(PublicationStatus.ACTIVE);
		highmedOrganization.setContent(CodeSystemContentMode.COMPLETE);
		highmedOrganization.setUrl("http://highmed.org/fhir/CodeSystem/highmed-organization");
		highmedOrganization.setVersion("0.5.0");
		highmedOrganization.addConcept().setCode("Test");

		validationSupport.addOrReplaceCodeSystem(highmedOrganization);
	}
}
