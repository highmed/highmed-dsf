package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

public class ReferenceResolverIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testUnknownLogicalReference() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInput();

		getExternalWebserviceClient().create(task);
	}

	@Test
	public void testUnknownAbsoluteLiteralExternalReference() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithAbsoluteLiteralExternalReferenceInput();

		getExternalWebserviceClient().create(task);
	}

	@Test
	public void testUnknownLogicalReferenceInBundle() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInput();
		Bundle bundle = getBundle(task);

		getExternalWebserviceClient().postBundle(bundle);
	}

	@Test
	public void testUnknownAbsoluteLiteralExternalReferenceInBundle() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithAbsoluteLiteralExternalReferenceInput();
		Bundle bundle = getBundle(task);

		getExternalWebserviceClient().postBundle(bundle);
	}

	@Test
	public void testKnownLogicalReferenceBasedOnKnownNamingSystem() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInputBasedOnKnownOrganizationNamingSystem("Test_Organization");

		getExternalWebserviceClient().create(task);
	}

	@Test
	public void testKnownLogicalReferenceMissingTypeInformationBasedOnKnownNamingSystem() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInputMissingTypeInformationBasedOnKnownOrganizationNamingSystem(
				"Test_Organization");

		expectForbidden(() -> getExternalWebserviceClient().create(task));
	}

	@Test
	public void testUnknownLogicalReferenceBasedOnKnownNamingSystem() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInputBasedOnKnownOrganizationNamingSystem("Test_Organization_Unknown");

		expectForbidden(() -> getExternalWebserviceClient().create(task));
	}

	@Test
	public void testUnknownLogicalReferenceBasedOnUnknownNamingSystem() throws Exception
	{
		prepareServerWithActivityDefinitionAndTaskProfile();

		Task task = getTaskWithLogicalReferenceInputBasedOnUnknownNamingSystem();

		getExternalWebserviceClient().create(task);
	}

	private void prepareServerWithActivityDefinitionAndTaskProfile() throws IOException
	{
		ActivityDefinition ad1 = readActivityDefinition();
		ActivityDefinition createdAd1 = getWebserviceClient().create(ad1);
		assertNotNull(createdAd1);
		assertNotNull(createdAd1.getIdElement().getIdPart());

		StructureDefinition testTaskProfile = readTestTaskProfile();
		StructureDefinition createdTestTaskProfile = getWebserviceClient().create(testTaskProfile);
		assertNotNull(createdTestTaskProfile);
		assertNotNull(createdTestTaskProfile.getIdElement().getIdPart());
	}

	private ActivityDefinition readActivityDefinition() throws IOException
	{
		try (InputStream in = Files.newInputStream(
				Paths.get("src/test/resources/integration/task/highmed-test-activity-definition2-0.5.0.xml")))
		{
			return fhirContext.newXmlParser().parseResource(ActivityDefinition.class, in);
		}
	}

	private StructureDefinition readTestTaskProfile() throws IOException
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/integration/task/highmed-test-task-profile-0.5.0.xml")))
		{
			return fhirContext.newXmlParser().parseResource(StructureDefinition.class, in);
		}
	}

	private Bundle getBundle(Task task)
	{
		Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(task).getRequest()
				.setUrl(ResourceType.Task.name()).setMethod(Bundle.HTTPVerb.POST);

		return bundle;
	}

	private Task getTaskWithLogicalReferenceInput() throws IOException
	{
		Task task = readTestTask();
		task.addInput()
				.setValue(new Reference().setType(ResourceType.Patient.name())
						.setIdentifier(new Identifier().setSystem("http://foo.bar/sid/pseudonym")
								.setValue(UUID.randomUUID().toString())))
				.getType().addCoding().setSystem("http://foo.bar/fhir/CodeSystem/test").setCode("patient");

		return task;
	}

	private Task getTaskWithAbsoluteLiteralExternalReferenceInput() throws IOException
	{
		Task task = readTestTask();
		task.addInput().setValue(new Reference("http://foo.bar/fhir/Patient/" + UUID.randomUUID().toString())).getType()
				.addCoding().setSystem("http://foo.bar/fhir/CodeSystem/test").setCode("patient");

		return task;
	}

	private Task getTaskWithLogicalReferenceInputBasedOnKnownOrganizationNamingSystem(
			String organizationIdentifierValue) throws IOException
	{
		Task task = readTestTask();
		task.addInput()
				.setValue(new Reference().setType(ResourceType.Organization.name())
						.setIdentifier(new Identifier().setSystem("http://highmed.org/sid/organization-identifier")
								.setValue(organizationIdentifierValue)))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("COS");

		return task;
	}

	private Task getTaskWithLogicalReferenceInputMissingTypeInformationBasedOnKnownOrganizationNamingSystem(
			String organizationIdentifierValue) throws IOException
	{
		Task task = readTestTask();
		task.addInput().setValue(new Reference().setIdentifier(new Identifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue(organizationIdentifierValue)))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/organization-role").setCode("COS");

		return task;
	}

	private Task getTaskWithLogicalReferenceInputBasedOnUnknownNamingSystem() throws IOException
	{
		Task task = readTestTask();
		task.addInput()
				.setValue(new Reference().setType(ResourceType.Patient.name()).setIdentifier(
						new Identifier().setSystem("http://foo.bar/fhir/sid/test").setValue("source/original")))
				.getType().addCoding().setSystem("http://foo.bar/fhir/CodeSystem/test").setCode("patient");

		return task;
	}

	private Task readTestTask() throws IOException
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/integration/task/highmed-test-task-0.5.0.xml")))
		{
			return fhirContext.newXmlParser().parseResource(Task.class, in).setAuthoredOn(new Date());
		}
	}
}
