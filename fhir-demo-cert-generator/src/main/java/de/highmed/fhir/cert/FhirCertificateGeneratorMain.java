package de.highmed.fhir.cert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import de.rwh.utils.crypto.CertificateAuthority;
import de.rwh.utils.crypto.CertificationRequestBuilder;
import de.rwh.utils.crypto.io.CertificateWriter;
import de.rwh.utils.crypto.io.PemIo;

public class FhirCertificateGeneratorMain
{
	static
	{
		CertificateAuthority.registerBouncyCastleProvider();
	}

	public static Properties read(Path propertiesFile, Charset encoding)
	{
		Properties properties = new Properties();

		try (Reader reader = new InputStreamReader(Files.newInputStream(propertiesFile), encoding))
		{
			properties.load(reader);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return properties;
	}

	public static void main(String[] args) throws Exception
	{
		Path caPem = Paths.get("cert/testca_certificate.pem");
		Path serverP12 = Paths.get("cert/localhost_certificate.p12");
		Path clientP12 = Paths.get("cert/test-client_certificate.p12");

		boolean fileExists = false;
		if (Files.exists(caPem))
		{
			fileExists = true;
			System.err.println("CA cert file already exists at " + caPem.toString());
		}
		if (Files.exists(serverP12))
		{
			fileExists = true;
			System.err.println("Server cert file already exists at " + serverP12.toString());
		}
		if (Files.exists(clientP12))
		{
			fileExists = true;
			System.err.println("Client cert file already exists at " + clientP12.toString());
		}

		if (fileExists)
			System.exit(-1);

		CertificateAuthority ca = new CertificateAuthority("DE", null, null, null, null, "Test-CA");

		System.out.println("Generating 4096 bit key pair for Test-CA");
		ca.initialize();
		X509Certificate caCertificate = ca.getCertificate();

		System.out.println("Saving CA cert to " + caPem.toAbsolutePath().toString());
		PemIo.writeX509CertificateToPem(caCertificate, caPem);

		X500Name serverSubject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, "test-server");
		System.out.println("Generating 4096 bit key pair for server cert");
		KeyPair serverRsaKeyPair = CertificationRequestBuilder.createRsaKeyPair4096Bit();
		JcaPKCS10CertificationRequest serverRequest = CertificationRequestBuilder
				.createServerCertificationRequest(serverSubject, serverRsaKeyPair, null, "localhost");

		System.out.println("Signing server cert");
		X509Certificate serverCertificate = ca.signWebServerCertificate(serverRequest);

		System.out.println("Saving server cert to " + serverP12.toAbsolutePath().toString());
		CertificateWriter.toPkcs12(serverP12, serverRsaKeyPair.getPrivate(), "password", serverCertificate,
				caCertificate, "test-server");

		X500Name clientSubject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, "test-client");
		System.out.println("Generating 4096 bit key pair for client cert");
		KeyPair clientRsaKeyPair = CertificationRequestBuilder.createRsaKeyPair4096Bit();
		JcaPKCS10CertificationRequest clientRequest = CertificationRequestBuilder
				.createClientCertificationRequest(clientSubject, clientRsaKeyPair);

		System.out.println("Signing client cert");
		X509Certificate clientCertificate = ca.signWebClientCertificate(clientRequest);

		System.out.println("Saving client cert to " + clientP12.toAbsolutePath().toString());
		CertificateWriter.toPkcs12(clientP12, clientRsaKeyPair.getPrivate(), "password", clientCertificate,
				caCertificate, "test-client");
	}
}
