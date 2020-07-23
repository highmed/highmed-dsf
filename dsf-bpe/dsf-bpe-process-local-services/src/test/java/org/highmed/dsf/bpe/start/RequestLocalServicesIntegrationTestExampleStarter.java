package org.highmed.dsf.bpe.start;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.ws.rs.WebApplicationException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.dsf.fhir.variables.BloomFilterConfig;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class RequestLocalServicesIntegrationTestExampleStarter
{
	public static void main(String[] args)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		char[] keyStorePassword = "password".toCharArray();
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(
				"dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12"),
				keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());
		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://medic1/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context, referenceCleaner);

		try
		{
			Task task = createTask(true, false);
			client.withMinimalReturn().create(task);
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

	private static Task createTask(boolean needsConsentCheck, boolean needsRecordLinkage)
			throws NoSuchAlgorithmException
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta()
				.addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-local-services-integration");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/localServicesIntegration/0.2.0");
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
				.setSystem(ConstantsBase.CODESYSTEM_QUERY_TYPE).setCode(ConstantsBase.CODESYSTEM_QUERY_TYPE_AQL);
		task.addInput().setValue(new BooleanType(needsConsentCheck)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-consent-check");
		task.addInput().setValue(new BooleanType(needsRecordLinkage)).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/feasibility").setCode("needs-record-linkage");

		if (needsRecordLinkage)
		{
			BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
			BloomFilterConfig bloomFilterConfig = new BloomFilterConfig(new Random().nextLong(),
					KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider).generateKey(),
					KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider).generateKey());

			task.addInput().setValue(new Base64BinaryType(bloomFilterConfig.toBytes())).getType().addCoding()
					.setSystem(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY)
					.setCode(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);
		}

		return task;
	}
}
