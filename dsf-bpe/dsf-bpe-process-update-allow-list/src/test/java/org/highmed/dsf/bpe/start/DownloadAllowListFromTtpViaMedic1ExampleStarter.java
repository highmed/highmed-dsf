package org.highmed.dsf.bpe.start;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class DownloadAllowListFromTtpViaMedic1ExampleStarter
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

		FhirWebserviceClient ttpClient = new FhirWebserviceClientJersey("https://ttp/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context, referenceCleaner);

		Bundle searchResult = ttpClient.searchWithStrictHandling(Bundle.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));
		if (searchResult.getTotal() != 1 && searchResult.getEntryFirstRep().getResource() instanceof Bundle)
			throw new IllegalStateException("Expected a single allow list Bundle");
		Bundle allowList = (Bundle) searchResult.getEntryFirstRep().getResource();

		System.out.println(context.newXmlParser().encodeResourceToString(allowList));

		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://medic1/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context, referenceCleaner);

		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-download-allow-list");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/downloadAllowList/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_MeDIC_1");

		task.addInput().setValue(new StringType("downloadAllowListMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");
		task.addInput()
				.setValue(new Reference(new IdType("https://ttp/fhir", "Bundle", allowList.getIdElement().getIdPart(),
						allowList.getIdElement().getVersionIdPart())))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-allow-list")
				.setCode("highmed_allow_list");

		client.withMinimalReturn().create(task);
	}
}
