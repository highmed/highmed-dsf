package org.highmed.dsf.bpe.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.highmed.dsf.bpe.client.WebserviceClient;
import org.highmed.dsf.bpe.client.WebserviceClientJersey;

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

//		WebserviceClient client = new WebserviceClientJersey("https://localhost:8002/bpe", trustStore, keyStore,
//				keyStorePassword, null, null, null, 0, 0, null);
		WebserviceClient client = new WebserviceClientJersey("https://ttp:8443/bpe", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null);

		client.startProcessWithVersion("ping", "1.0.0");

//		client.startProcessWithVersion("updateWhiteList", "1.0.0");

//		client.startProcessWithVersion("requestUpdateResources", "1.0.0", Map.of("target-identifier",
//				Collections.singletonList("http://highmed.org/fhir/CodeSystem/organization|"), "bundle-id",
//				Arrays.asList("Bundle/30a10cba-64e3-4340-8930-35fa5e0fd69d")));

//		client.startProcessWithVersion("requestUpdateResources", "1.0.0", Map.of("target-identifier",
//				Collections.singletonList("http://highmed.org/fhir/CodeSystem/organization|"), "bundle-id",
//				Arrays.asList("Bundle/30a376fa-f3ba-4f68-8e04-c5b9c0c4f5c9")));
	}
}
