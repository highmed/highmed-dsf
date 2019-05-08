package org.highmed.bpe.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class TestBpeJerseyClient
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		String keyStorePassword = "password";
		KeyStore keyStore = CertificateReader
				.fromPkcs12(Paths.get("../bpe-demo-cert-generator/cert/test-client_certificate.p12"), keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		WebserviceClient client = new WebserviceClientJersey("https://localhost:8002/bpe", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null);

		// try
		// {
		// client.startProcessWithVersion("ping", "1.0.0");
		// }
		// catch (WebApplicationException e)
		// {
		// e.printStackTrace();
		// }

		client.startProcessWithVersion("requestUpdateResources", "1.0.0", Map.of("target-identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/organization|"), "resource-criteria",
				Arrays.asList(
						"Organization?identifier=http://highmed.org/fhir/CodeSystem/organization|Test_Organization",
						"Endpoint?organization:identifier=http://highmed.org/fhir/CodeSystem/organization|Test_Organization",
						"Organization?identifier=http://highmed.org/fhir/CodeSystem/organization|Foo_Organization",
						"Endpoint?organization:identifier=http://highmed.org/fhir/CodeSystem/organization|Foo_Organization")));
	}
}
