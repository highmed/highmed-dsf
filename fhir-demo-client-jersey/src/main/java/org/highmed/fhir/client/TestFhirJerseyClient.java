package org.highmed.fhir.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Organization;
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
		KeyStore keyStore = CertificateReader
				.fromPkcs12(Paths.get("C:/Users/hhund/hhn/test-ca/test-client_certificate.p12"), keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirJerseyClient fhirJerseyClient = new FhirJerseyClient("https://localhost:8001/fhir", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, FhirContext.forR4());

		// Patient patient = new Patient();
		// patient.setIdElement(new IdType("Patient", UUID.randomUUID().toString(), "2"));
		// fhirJerseyClient.create(patient);

		DomainResource organization = fhirJerseyClient.create(new Organization().setName("Test Organization"));
		fhirJerseyClient.create(new Task().setRequester(new Reference(organization.getIdElement().toVersionless()))
				.setDescription("Organization reference without version").setAuthoredOn(new Date())
				.setStatus(TaskStatus.REQUESTED));
		fhirJerseyClient.create(new Task().setRequester(new Reference(organization.getIdElement()))
				.setDescription("Organization reference with version"));

		// fhirJerseyClient.getConformance();
	}
}
