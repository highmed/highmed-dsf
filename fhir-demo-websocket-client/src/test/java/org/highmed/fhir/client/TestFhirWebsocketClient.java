package org.highmed.fhir.client;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class TestFhirWebsocketClient
{
	private static final Logger logger = LoggerFactory.getLogger(TestFhirWebsocketClient.class);

	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InterruptedException
	{
		String keyStorePassword = "password";
		KeyStore keyStore = CertificateReader.fromPkcs12(
				Paths.get("../fhir-demo-cert-generator/cert/test-client_certificate.p12"), keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext fhirContext = FhirContext.forR4();

		WebsocketClientTyrus client = new WebsocketClientTyrus(fhirContext, URI.create("wss://localhost:8001/fhir/ws"),
				trustStore, keyStore, keyStorePassword, "8c52c90d-a99c-40ce-9e49-8ba604224401");

		client.connect();
		client.setDomainResourceHandler(r -> onResource(fhirContext, r), fhirContext::newJsonParser);

		Thread.sleep(30_000);
	}

	private static void onResource(FhirContext fhirContext, DomainResource resource)
	{
		logger.debug("Resource received\n{}",
				fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource));
	}
}
