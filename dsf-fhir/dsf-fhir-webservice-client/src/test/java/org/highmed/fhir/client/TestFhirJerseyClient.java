package org.highmed.fhir.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class TestFhirJerseyClient
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		String keyStorePassword = "password";
		KeyStore keyStore = CertificateReader.fromPkcs12(
				Paths.get("../../dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12"), keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://localhost:8001/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context);

		try
		{
			// Bundle bundle = new Bundle();
			// bundle.setType(BundleType.TRANSACTION);
			//
			// Organization organization = new Organization();
			// organization.setId(UUID.randomUUID().toString());
			// organization.setName("Test Organization");
			//
			// Endpoint endpoint = new Endpoint();
			// endpoint.setId(UUID.randomUUID().toString());
			// endpoint.setName("Test Endpoint");
			// endpoint.setManagingOrganization(new Reference("urn:uuid:" + organization.getIdElement().getIdPart()));
			//
			// organization.addEndpoint(new Reference("urn:uuid:" + endpoint.getIdElement().getIdPart()));
			//
			// BundleEntryComponent entry1 = bundle.addEntry();
			// entry1.setResource(organization);
			// entry1.setFullUrl("urn:uuid:" + organization.getIdElement().getIdPart());
			// entry1.getRequest().setMethod(HTTPVerb.POST);
			// entry1.getRequest().setUrl("Organization");
			//
			// BundleEntryComponent entry2 = bundle.addEntry();
			// entry2.setResource(endpoint);
			// entry2.setFullUrl("urn:uuid:" + endpoint.getIdElement().getIdPart());
			// entry2.getRequest().setMethod(HTTPVerb.POST);
			// entry2.getRequest().setUrl("Endpoint");
			//
			// System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
			//

			// client.postBundle(bundle);

			// Patient patient = new Patient();
			// patient.setIdElement(new IdType("Patient", UUID.randomUUID().toString(), "2"));
			// Patient createdPatient = client.create(patient);
			//
			// createdPatient.setGender(AdministrativeGender.FEMALE);
			// client.update(createdPatient);

			//
			// Organization organization = client.create(Organization.class,
			// new Organization().setName("Test Organization"));
			//
			// ResearchStudy researchStudy = client.create(ResearchStudy.class,
			// new ResearchStudy().setDescription("Test Research Study").setSponsor(new Reference(organization)));
			//
			// Task task = new Task();
			// task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/DataSharingTask");
			// task.setRequester(new Reference(organization.getIdElement()));
			// task.setDescription("Organization reference with version");
			// task.setAuthoredOn(new Date());
			// task.setStatus(TaskStatus.REQUESTED);
			// task.setIntent(TaskIntent.ORDER);
			// Extension ext = task.addExtension();
			// ext.setUrl("http://hl7.org/fhir/StructureDefinition/workflow-researchStudy");
			// Reference researchStudyReference = new Reference(researchStudy);
			// ext.setValue(researchStudyReference);
			//
			// client.create(task);
			//
			// client.create(
			// new Task().setRequester(new Reference(organization.getIdElement().toVersionless()))
			// .setDescription("Organization reference without version"));
			//

			// CapabilityStatement conformance = client.getConformance();
			// System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(conformance));

			// StructureDefinition sD = context.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/profiles/extension-workflow-researchstudy.xml")));
			// client.create(sD);

			// StructureDefinition sD = context.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/profiles/task-highmed-0.0.2.xml")));
			// client.create(sD);

			// StructureDefinition sd = client
			// .generateSnapshot("http://highmed.org/fhir/StructureDefinition/DataSharingTask");
			// String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// StructureDefinition diff = context.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/task-highmed-0.0.1.xml")));
			// StructureDefinition sd = client.generateSnapshot(diff);
			// String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// StructureDefinition diff = context.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/address-de-basis-0.2.xml")));
			// StructureDefinition sd = client.generateSnapshot(diff.setSnapshot(null));
			// String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// Subscription subscription = new Subscription();
			// subscription.setStatus(SubscriptionStatus.ACTIVE);
			// subscription.setReason("Test");
			// subscription.setCriteria("Task");
			// SubscriptionChannelComponent channel = subscription.getChannel();
			// channel.setType(SubscriptionChannelType.WEBSOCKET);
			// channel.setPayload(Constants.CT_FHIR_JSON_NEW);
			//
			// client.create(subscription);

			// Task createdTask = client.create(new Task().setDescription("Status draft").setStatus(TaskStatus.DRAFT));
			//
			// createdTask.setStatus(TaskStatus.REQUESTED).setDescription("Status requested");
			// client.update(createdTask);

			// Organization org = client.read(Organization.class, "e8aa9c06-9789-4c2b-8292-1c2a9601c2cc");
			//
			// Endpoint endpoint = new Endpoint();
			// endpoint.setStatus(EndpointStatus.ACTIVE);
			// endpoint.setConnectionType(new Coding("http://terminology.hl7.org/CodeSystem/endpoint-connection-type",
			// "hl7-fhir-rest", "HL7 FHIR"));
			// endpoint.setManagingOrganization(
			// new Reference(new IdType(org.getIdElement().getResourceType(), org.getIdElement().getIdPart())));
			// endpoint.setAddress("https://localhost:8001/fhir");
			// endpoint.addPayloadType(new CodeableConcept(new Coding("http://hl7.org/fhir/resource-types", "Task",
			// "Task")));
			// endpoint.addPayloadMimeType(Constants.CT_FHIR_JSON_NEW);
			// endpoint.addPayloadMimeType(Constants.CT_FHIR_XML_NEW);
			//
			// Endpoint createdEndpoint = client.create(endpoint);
			//
			// org.getEndpoint().clear();
			// org.addEndpoint(new Reference(new IdType(createdEndpoint.getIdElement().getResourceType(),
			// createdEndpoint.getIdElement().getIdPart())));
			// client.update(org);
			//
			// Organization org = client.read(Organization.class, "e8aa9c06-9789-4c2b-8292-1c2a9601c2cc");
			// org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/organizations").setValue("Hochschule
			// Heilbronn");
			//
			// client.update(org);

			// Organization org = new Organization();
			// org.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/organization")
			// .setValue("Test Organization");
			// org.setActive(true);
			// org.setName("Test Organization");
			// org = client.create(org);
			//
			// Endpoint endpoint1 = new Endpoint();
			// endpoint1.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/endpoint").setValue("Endpoint1");
			// endpoint1.setStatus(EndpointStatus.ACTIVE);
			// endpoint1.setConnectionType(new Coding("http://terminology.hl7.org/CodeSystem/endpoint-connection-type",
			// "hl7-fhir-rest", "HL7 FHIR"));
			// endpoint1.setManagingOrganization(
			// new Reference(new IdType(org.getIdElement().getResourceType(), org.getIdElement().getIdPart())));
			// endpoint1.setAddress("https://localhost:8001/fhir");
			// endpoint1.addPayloadType(
			// new CodeableConcept(new Coding("http://hl7.org/fhir/resource-types", "Task", "Task")));
			// endpoint1.addPayloadMimeType(Constants.CT_FHIR_JSON_NEW);
			// endpoint1.addPayloadMimeType(Constants.CT_FHIR_XML_NEW);
			// endpoint1 = client.create(endpoint1);
			//
			// Endpoint endpoint2 = new Endpoint();
			// endpoint2.addIdentifier().setSystem("http://highmed.org/fhir/CodeSystem/endpoint").setValue("Endpoint2");
			// endpoint2.setStatus(EndpointStatus.ACTIVE);
			// endpoint2.setConnectionType(new Coding("http://terminology.hl7.org/CodeSystem/endpoint-connection-type",
			// "hl7-fhir-rest", "HL7 FHIR"));
			// endpoint2.setManagingOrganization(
			// new Reference(new IdType(org.getIdElement().getResourceType(), org.getIdElement().getIdPart())));
			// endpoint2.setAddress("https://localhost:8001/fhir");
			// endpoint2.addPayloadType(
			// new CodeableConcept(new Coding("http://hl7.org/fhir/resource-types", "Task", "Task")));
			// endpoint2.addPayloadMimeType(Constants.CT_FHIR_JSON_NEW);
			// endpoint2.addPayloadMimeType(Constants.CT_FHIR_XML_NEW);
			// endpoint2 = client.create(endpoint2);
			//
			// org.addEndpoint(new Reference(
			// new IdType(endpoint1.getIdElement().getResourceType(), endpoint1.getIdElement().getIdPart())));
			// org.addEndpoint(new Reference(
			// new IdType(endpoint2.getIdElement().getResourceType(), endpoint2.getIdElement().getIdPart())));
			//
			// client.update(org);

			// Organization org = new Organization();
			// client.create(org, "identifier=http://highmed.org/fhir/CodeSystem/organization|Test
			// Organization&_format=json");

			// Organization org = new Organization();
			// org.setName("conditional update");
			// client.create(org);

			//
			// Patient created = client.create(new Patient().setActive(true).setBirthDate(new Date()));
			//
			// var bundle = new Bundle();
			// bundle.setType(BundleType.TRANSACTION);
			//
			// var del1 = bundle.addEntry();
			// del1.getRequest().setMethod(HTTPVerb.DELETE);
			// del1.getRequest().setUrl("Patient?_id=bbdc562a-3c15-4641-bf8b-e72e7380548c");
			//
			// var del2 = bundle.addEntry();
			// del2.getRequest().setMethod(HTTPVerb.DELETE);
			// del2.getRequest().setUrl("Patient/bbdc562a-3c15-4641-bf8b-e72e7380548c");
			//

			// client.deleteConditionaly(Organization.class,
			// Map.of("name:exact", Collections.singletonList("Transaction Test Organization")));
			// client.deleteConditionaly(Endpoint.class,
			// Map.of("name:exact", Collections.singletonList("Transaction Test Endpoint")));

			// deleteCreateUpdateReadTaskBundleTest(context, client);
			// updateBundleTest(context, client);
			// deleteOrganizationsAndEndpoints(context, client);

			client.create(patientUpdateOrCreateBundle());
		}
		catch (WebApplicationException e)
		{
			if (e.getResponse() != null && e.getResponse().hasEntity())
			{
				OperationOutcome outcome = e.getResponse().readEntity(OperationOutcome.class);
				String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome);
				System.out.println(xml);
			}
			else
				e.printStackTrace();
		}
	}

	private static void deleteOrganizationsAndEndpoints(FhirContext context, FhirWebserviceClient client)
	{
		Bundle orgs = client.search(Organization.class, Collections.emptyMap());
		orgs.getEntry().stream().filter(e -> e.getResource() instanceof Organization)
				.map(e -> (Organization) e.getResource()).map(o -> o.getIdElement().getIdPart())
				.forEach(id -> client.delete(Organization.class, id));

		Bundle epts = client.search(Endpoint.class, Collections.emptyMap());
		epts.getEntry().stream().filter(e -> e.getResource() instanceof Endpoint).map(e -> (Endpoint) e.getResource())
				.map(o -> o.getIdElement().getIdPart()).forEach(id -> client.delete(Endpoint.class, id));

	}

	private static void deleteCreateUpdateReadTaskBundleTest(FhirContext context, FhirWebserviceClient client)
	{
		final String taskIdentifierSystem = "http://highmed.org/fhir/CodeSystem/task";
		final String taskIdentifierValue = "Transaction Update Test Task";

		var bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);

		var deleleteTaskentry = bundle.addEntry();
		deleleteTaskentry.getRequest().setMethod(HTTPVerb.DELETE);
		deleleteTaskentry.getRequest().setUrl("Task?identifier=" + taskIdentifierSystem + "|" + taskIdentifierValue);

		var task = new Task();
		task.setAuthoredOn(new Date());
		task.setDescription("Transaction Update Test Task");
		task.addIdentifier().setSystem(taskIdentifierSystem).setValue(taskIdentifierValue);

		var createTaskEntry = bundle.addEntry();
		createTaskEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		createTaskEntry.setResource(task);
		createTaskEntry.getRequest().setMethod(HTTPVerb.POST);
		createTaskEntry.getRequest().setUrl("Task");
		createTaskEntry.getRequest().setIfNoneExist("identifier=" + taskIdentifierSystem + "|" + taskIdentifierValue);

		var updateTask = task.copy();
		updateTask.setStatus(TaskStatus.DRAFT);

		var updateTaskEntry = bundle.addEntry();
		updateTaskEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		updateTaskEntry.setResource(updateTask);
		updateTaskEntry.getRequest().setMethod(HTTPVerb.PUT);
		updateTaskEntry.getRequest().setUrl("Task/?identifier=" + taskIdentifierSystem + "|" + taskIdentifierValue);
		updateTaskEntry.getRequest().setIfMatch("W/\"1\"");

		var readTaskEntry = bundle.addEntry();
		readTaskEntry.getRequest().setMethod(HTTPVerb.GET);
		// readTaskEntry.getRequest()
		// .setUrl("Task/?identifier=" + taskIdentifierSystem + "|" + taskIdentifierValue + "&foo=bar");
		readTaskEntry.getRequest().setUrl(createTaskEntry.getFullUrl());

		System.out.println(
				"request bundle:\n" + context.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
		Bundle result = client.postBundle(bundle);
		System.out.println(
				"result bundle:\n" + context.newXmlParser().setPrettyPrint(true).encodeResourceToString(result));
	}

	private static void deleteCreateBundleTest(FhirContext context, FhirWebserviceClient client)
	{
		var bundle = deleteCreateBundle();

		System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
		Bundle result = client.postBundle(bundle);
		System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(result));
	}

	private static Bundle deleteCreateBundle()
	{
		final String orgIdentifierSystem = "http://highmed.org/fhir/CodeSystem/organization";
		final String orgIdentifierValue = "Transaction Test Organization";
		final String eptIdentifierSystem = "http://highmed.org/fhir/CodeSystem/endpoint";
		final String eptIdentifierValue = "Transaction Test Endpoint";

		var bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);

		var delOrg = bundle.addEntry();
		delOrg.getRequest().setMethod(HTTPVerb.DELETE);
		delOrg.getRequest().setUrl("Organization?identifier=" + orgIdentifierSystem + "|" + orgIdentifierValue);

		var delEpt = bundle.addEntry();
		delEpt.getRequest().setMethod(HTTPVerb.DELETE);
		delEpt.getRequest().setUrl("Endpoint?identifier=" + eptIdentifierSystem + "|" + eptIdentifierValue);

		var org = new Organization();
		org.setName("Transaction Test Organization");
		org.addIdentifier().setSystem(orgIdentifierSystem).setValue(orgIdentifierValue);
		Reference eptReference = new Reference();
		eptReference.setType("Endpoint");
		eptReference.getIdentifier().setSystem(eptIdentifierSystem);
		eptReference.getIdentifier().setValue(eptIdentifierValue);
		org.addEndpoint(eptReference);

		var ept = new Endpoint();
		ept.setName("Transaction Test Endpoint");
		ept.addIdentifier().setSystem(eptIdentifierSystem).setValue(eptIdentifierValue);
		Reference orgReference = new Reference();
		orgReference.setType("Organization");
		orgReference.getIdentifier().setSystem(orgIdentifierSystem);
		orgReference.getIdentifier().setValue(orgIdentifierValue);
		ept.setManagingOrganization(orgReference);

		var orgEntry = bundle.addEntry();
		orgEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		orgEntry.setResource(org);
		orgEntry.getRequest().setMethod(HTTPVerb.POST);
		orgEntry.getRequest().setUrl(org.getResourceType().name());
		orgEntry.getRequest().setIfNoneExist("identifier=" + orgIdentifierSystem + "|" + orgIdentifierValue);

		var eptEntry = bundle.addEntry();
		eptEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		eptEntry.setResource(ept);
		eptEntry.getRequest().setMethod(HTTPVerb.POST);
		eptEntry.getRequest().setUrl(ept.getResourceType().name());
		eptEntry.getRequest().setIfNoneExist("identifier=" + eptIdentifierSystem + "|" + eptIdentifierValue);
		return bundle;
	}

	private static void updateBundleTest(FhirContext context, FhirWebserviceClient client)
	{
		final String orgIdentifierSystem = "http://highmed.org/fhir/CodeSystem/organization";
		final String orgIdentifierValue = "Transaction Test Organization";
		final String eptIdentifierSystem = "http://highmed.org/fhir/CodeSystem/endpoint";
		final String eptIdentifierValue = "Transaction Test Endpoint";

		var bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);

		var org = new Organization();
		org.setName("Transaction Test Organization");
		org.addIdentifier().setSystem(orgIdentifierSystem).setValue(orgIdentifierValue);
		Reference eptReference = new Reference();
		eptReference.setType("Endpoint");
		eptReference.getIdentifier().setSystem(eptIdentifierSystem);
		eptReference.getIdentifier().setValue(eptIdentifierValue);
		org.addEndpoint(eptReference);

		var ept = new Endpoint();
		ept.setName("Transaction Test Endpoint");
		ept.addIdentifier().setSystem(eptIdentifierSystem).setValue(eptIdentifierValue);
		Reference orgReference = new Reference();
		orgReference.setType("Organization");
		orgReference.getIdentifier().setSystem(orgIdentifierSystem);
		orgReference.getIdentifier().setValue(orgIdentifierValue);
		ept.setManagingOrganization(orgReference);

		var orgEntry = bundle.addEntry();
		orgEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		orgEntry.setResource(org);
		orgEntry.getRequest().setMethod(HTTPVerb.PUT);
		orgEntry.getRequest().setUrl("Organization?identifier=" + orgIdentifierSystem + "|" + orgIdentifierValue);

		var eptEntry = bundle.addEntry();
		eptEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		eptEntry.setResource(ept);
		eptEntry.getRequest().setMethod(HTTPVerb.PUT);
		eptEntry.getRequest().setUrl("Endpoint?identifier=" + eptIdentifierSystem + "|" + eptIdentifierValue);

		System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
		Bundle result = client.postBundle(bundle);
		System.out.println(context.newXmlParser().setPrettyPrint(true).encodeResourceToString(result));
	}

	private static Bundle patientUpdateOrCreateBundle()
	{
		final String patIdSystem = "http://foo.bar/baz";
		final String patIdValue = "123467890";

		var b = new Bundle();
		b.setType(BundleType.TRANSACTION);

		var p = new Patient();
		p.setActive(true);
		var i = p.addIdentifier();
		i.setSystem(patIdSystem);
		i.setValue(patIdValue);

		var e = b.addEntry();
		e.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		e.setResource(p);
		e.getRequest().setMethod(HTTPVerb.PUT);
		e.getRequest().setUrl("Patient?identifier=" + patIdSystem + "|" + patIdValue);

		return b;
	}
}
