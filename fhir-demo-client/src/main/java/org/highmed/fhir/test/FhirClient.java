package org.highmed.fhir.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import de.rwh.utils.crypto.io.CertificateReader;

public class FhirClient
{
	private static final Logger logger = LoggerFactory.getLogger(FhirClient.class);

	public static void main(String[] args)
	{
		FhirContext context = getFhirContext();
		IGenericClient client = context.newRestfulGenericClient("https://localhost:8001/fhir");

		// IFetchConformanceUntyped capabilities = client.capabilities();
		// CapabilityStatement capabilityStatement = capabilities.ofType(CapabilityStatement.class).execute();
		//
		// System.out.println(capabilityStatement.getVersion());

		Task task = new Task();
		task.setInstantiatesUri("bpmn://highmed.org/ResourceSharing/v0/TaskId");
		
		MethodOutcome outcome = client.create().resource(task).encodedXml().execute();
		logger.info("Task created ... " + outcome.getId());

		Task task1 = client.read().resource(Task.class).withId(outcome.getId().getIdPart()).execute();
		logger.info("Task read 1 ... " + task1.getId());

		Task task2 = client.read().resource(Task.class).withId(outcome.getId()).execute();
		logger.info("Task read 2 ... " + task2.getId());

		try
		{
			client.read().resource(Task.class).withId("123").execute();
		}
		catch (ResourceNotFoundException e)
		{
			logger.error(e.getMessage());
		}

	}

	private static FhirContext getFhirContext()
	{
		try
		{
			String keyStorePassword = "password";
			KeyStore keyStore = CertificateReader
					.fromPkcs12(Paths.get("C:/Users/hhund/hhn/test-ca/test-client_certificate.p12"), keyStorePassword);
			// KeyStore trustStore = CertificateHelper.extractTrust(keyStore);
			KeyStore trustStore = CertificateReader
					.allFromCer(Paths.get("C:/Users/hhund/hhn/test-ca/ca/ca/testca_certificate.pem"));

			FhirContext context = FhirContext.forR4();
			context.setRestfulClientFactory(
					new ApacheRestfulClientFactoryWithTls(context, trustStore, keyStore, keyStorePassword));

			return context;
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
