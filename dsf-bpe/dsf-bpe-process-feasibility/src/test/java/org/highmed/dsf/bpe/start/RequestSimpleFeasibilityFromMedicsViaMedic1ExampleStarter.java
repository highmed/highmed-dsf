package org.highmed.dsf.bpe.start;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Expression.ExpressionLanguage;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class RequestSimpleFeasibilityFromMedicsViaMedic1ExampleStarter
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		String keyStorePassword = "password";
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(
				"../../dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12"),
				keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://medic1/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context);

		try
		{
			Group group1 = createGroup("Group 1");
			Group group2 = createGroup("Group 2");
			Practitioner practitioner = createPractitioner();
			PractitionerRole practitionerRole = createPractitionerRole(practitioner);
			ResearchStudy researchStudy = createResearchStudy(group1, group2, practitioner);
			Task task = createTask(practitioner, researchStudy);

			Bundle bundle = new Bundle();
			bundle.setType(BundleType.TRANSACTION);
			bundle.addEntry().setResource(group1).setFullUrl(group1.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Group");
			bundle.addEntry().setResource(group2).setFullUrl(group2.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Group");
			bundle.addEntry().setResource(practitioner).setFullUrl(practitioner.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Practitioner");
			bundle.addEntry().setResource(practitionerRole).setFullUrl(practitionerRole.getIdElement().getIdPart())
					.getRequest().setMethod(HTTPVerb.POST).setUrl("PractitionerRole");
			bundle.addEntry().setResource(researchStudy).setFullUrl(researchStudy.getIdElement().getIdPart())
					.getRequest().setMethod(HTTPVerb.POST).setUrl("ResearchStudy");
			bundle.addEntry().setResource(task).setFullUrl(task.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Task");

			client.postBundle(bundle);
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

	private static Group createGroup(String name)
	{
		Group group = new Group();
		group.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		group.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-group");
		group.getText().getDiv().addText("This is the description");
		group.getText().setStatus(Narrative.NarrativeStatus.ADDITIONAL);
		group.setType(GroupType.PERSON);
		group.setActual(true);
		group.setActive(true);
		group.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/query").setValue(new Expression()
				.setLanguage(ExpressionLanguage.APPLICATION_XFHIRQUERY.toCode()).setExpression("SELECT COUNT(e) FROM EHR e"));
		group.setName(name);

		return group;
	}

	private static Practitioner createPractitioner()
	{
		Practitioner practitioner = new Practitioner();
		practitioner.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		practitioner.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-practitioner");
		practitioner.getNameFirstRep().setFamily("HiGHmed").addGiven("Test");

		return practitioner;
	}

	private static PractitionerRole createPractitionerRole(Practitioner practitioner)
	{
		PractitionerRole practitionerRole = new PractitionerRole();
		practitionerRole.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		practitioner.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-practitioner-role");
		practitionerRole.getPractitioner().setReference(practitioner.getIdElement().getIdPart())
				.setType("Practitioner");
		practitionerRole.getOrganization().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/CodeSystem/organization").setValue("Test_MeDIC_1");

		return practitionerRole;
	}

	private static ResearchStudy createResearchStudy(Group group1, Group group2, Practitioner practitioner)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		researchStudy.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-research-study");
		researchStudy.setTitle("Research Study Test");
		researchStudy.setStatus(ResearchStudyStatus.ACTIVE);
		researchStudy.setDescription(
				"This is a test research study based on the highmed profile. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");
		researchStudy.addEnrollment().setReference(group1.getIdElement().getIdPart()).setType("Group");
		researchStudy.addEnrollment().setReference(group2.getIdElement().getIdPart()).setType("Group");
		researchStudy.getPrincipalInvestigator().setReference(practitioner.getIdElement().getIdPart())
				.setType("Practitioner");

		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/fhir/CodeSystem/organization").setValue("Test_MeDIC_1")));
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/fhir/CodeSystem/organization").setValue("Test_MeDIC_2")));
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference().setType("Organization").setIdentifier(new Identifier()
						.setSystem("http://highmed.org/fhir/CodeSystem/organization").setValue("Test_MeDIC_3")));

		return researchStudy;
	}

	private static Task createTask(Practitioner practitioner, ResearchStudy researchStudy)
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/1.0.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Practitioner").setReference(practitioner.getIdElement().getIdPart());
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/CodeSystem/organization").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput()
				.setValue(
						new Reference().setReference(researchStudy.getIdElement().getIdPart()).setType("ResearchStudy"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/feasibility")
				.setCode("research-study-reference");

		return task;
	}
}
