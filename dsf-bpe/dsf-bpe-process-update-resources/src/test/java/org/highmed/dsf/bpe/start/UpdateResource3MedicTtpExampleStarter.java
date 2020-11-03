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

public class UpdateResource3MedicTtpExampleStarter
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
		FhirWebserviceClient client = new FhirWebserviceClientJersey("https://ttp/fhir/", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, context, referenceCleaner);

		Bundle searchResult = client.searchWithStrictHandling(Bundle.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));
		if (searchResult.getTotal() != 1 && searchResult.getEntryFirstRep().getResource() instanceof Bundle)
			throw new IllegalStateException("Expected a single allow list Bundle");
		Bundle allowList = (Bundle) searchResult.getEntryFirstRep().getResource();

		System.out.println(context.newXmlParser().encodeResourceToString(allowList));

		Task task = new Task();
		task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-task-request-update-resources");
		task.setInstantiatesUri("http://highmed.org/bpe/Process/requestUpdateResources/0.3.0");
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");
		task.getRestriction().addRecipient().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("Test_TTP");

		task.addInput().setValue(new StringType("requestUpdateResourcesMessage")).getType().addCoding()
				.setSystem("http://highmed.org/fhir/CodeSystem/bpmn-message").setCode("message-name");

		task.addInput()
				.setValue(new Reference(new IdType("Bundle", allowList.getIdElement().getIdPart(),
						allowList.getIdElement().getVersionIdPart())))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-resources")
				.setCode("bundle-reference");

		task.addInput().setValue(new StringType("http://highmed.org/fhir/NamingSystem/organization-identifier|"))
				.getType().addCoding().setSystem("http://highmed.org/fhir/CodeSystem/update-resources")
				.setCode("organization-identifier-search-parameter");

		client.withMinimalReturn().create(task);
	}
}
