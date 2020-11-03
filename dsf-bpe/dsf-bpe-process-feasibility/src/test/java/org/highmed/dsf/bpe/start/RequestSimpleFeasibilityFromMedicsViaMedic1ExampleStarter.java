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

import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
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
		char[] keyStorePassword = "password".toCharArray();
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(
				"../../dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12"),
				keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());
		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://medic1/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context, referenceCleaner);

		try
		{
			Group group1 = createGroup("Group 1");
			Group group2 = createGroup("Group 2");
			ResearchStudy researchStudy = createResearchStudy(group1, group2);
			Task task = createTask(researchStudy);

			Bundle bundle = new Bundle();
			bundle.setType(BundleType.TRANSACTION);
			bundle.addEntry().setResource(group1).setFullUrl(group1.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Group");
			bundle.addEntry().setResource(group2).setFullUrl(group2.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Group");
			bundle.addEntry().setResource(researchStudy).setFullUrl(researchStudy.getIdElement().getIdPart())
					.getRequest().setMethod(HTTPVerb.POST).setUrl("ResearchStudy");
			bundle.addEntry().setResource(task).setFullUrl(task.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl("Task");

			client.withMinimalReturn().postBundle(bundle);
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
		group.setActual(false);
		group.setActive(true);
		group.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/query").setValue(
				new Expression().setLanguageElement(ConstantsBase.AQL_QUERY_TYPE)
						.setExpression("SELECT COUNT(e) FROM EHR e"));
		group.setName(name);

		return group;
	}

	private static ResearchStudy createResearchStudy(Group group1, Group group2)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		researchStudy.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-research-study-feasibility");
		researchStudy.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/research-study-identifier")
				.setValue(UUID.randomUUID().toString());
		researchStudy.setStatus(ResearchStudyStatus.ACTIVE);
		researchStudy.addEnrollment().setReference(group1.getIdElement().getIdPart());
		researchStudy.addEnrollment().setReference(group2.getIdElement().getIdPart());

		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic").setValue(
				new Reference().setType("Organization").setIdentifier(
						new Identifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
								.setValue("Test_MeDIC_1")));
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic").setValue(
				new Reference().setType("Organization").setIdentifier(
						new Identifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
								.setValue("Test_MeDIC_2")));
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic").setValue(
				new Reference().setType("Organization").setIdentifier(
						new Identifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
								.setValue("Test_MeDIC_3")));
		researchStudy.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp").setValue(
				new Reference().setType("Organization").setIdentifier(
						new Identifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
								.setValue("Test_TTP")));

		return researchStudy;
	}

	private static Task createTask(ResearchStudy researchStudy)
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestSimpleFeasibility/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("requestSimpleFeasibilityMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput().setValue(
				new Reference().setReference(researchStudy.getIdElement().getIdPart()).setType("ResearchStudy"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/feasibility")
				.setCode("research-study-reference");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");
		task.addInput().setValue(new BooleanType(true)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");

		return task;
	}
}
