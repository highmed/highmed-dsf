package org.highmed.dsf.bpe.cert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import de.rwh.utils.crypto.CertificateAuthority;
import de.rwh.utils.crypto.CertificationRequestBuilder;
import de.rwh.utils.crypto.io.CertificateWriter;
import de.rwh.utils.crypto.io.PemIo;

public class BpeTestCertificateGeneratorMain
{
	private static final String CERT_PASSWORD = "password";

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
		Path caCertPem = Paths.get("cert/testca_certificate.pem");
		Path caPrivateKey = Paths.get("cert/testca_private-key.pem");
		Path caPublicKey = Paths.get("cert/testca_public-key.pem");
		Path serverCertP12 = Paths.get("cert/localhost_certificate.p12");
		Path serverCertPem = Paths.get("cert/localhost_certificate.pem");
		Path serverPrivateKey = Paths.get("cert/localhost_private-key.pem");
		Path serverPublicKey = Paths.get("cert/localhost_public-key.pem");
		Path clientCertP12 = Paths.get("cert/test-client_certificate.p12");
		Path clientCertPem = Paths.get("cert/test-client_certificate.pem");
		Path clientPrivateKey = Paths.get("cert/test-client_private-key.pem");
		Path clientPublicKey = Paths.get("cert/test-client_public-key.pem");

		boolean fileExists = false;
		if (exists("CA certificate", caCertPem))
			fileExists = true;
		if (exists("CA private-key", caPrivateKey))
			fileExists = true;
		if (exists("CA public-key", caPublicKey))
			fileExists = true;
		if (exists("Server certificate (p12)", serverCertP12))
			fileExists = true;
		if (exists("Server certificate (pem)", serverCertPem))
			fileExists = true;
		if (exists("Server private-key", serverPrivateKey))
			fileExists = true;
		if (exists("Server public-key", serverPublicKey))
			fileExists = true;
		if (exists("Client certificate (p12)", clientCertP12))
			fileExists = true;
		if (exists("Client certificate (pem)", clientCertPem))
			fileExists = true;
		if (exists("Client private-key", clientPrivateKey))
			fileExists = true;
		if (exists("Client public-key", clientPublicKey))
			fileExists = true;

		if (fileExists)
			System.exit(-1);

		CertificateAuthority ca = new CertificateAuthority("DE", null, null, null, null, "Test-CA");

		System.out.println("Generating 4096 bit key pair for Test-CA");
		ca.initialize();
		X509Certificate caCertificate = ca.getCertificate();

		System.out.println("Saving CA certificate to " + caCertPem.toAbsolutePath().toString());
		PemIo.writeX509CertificateToPem(caCertificate, caCertPem);
		System.out.println("Saving CA private-key to " + caPrivateKey.toAbsolutePath().toString());
		PemIo.writePrivateKeyToPem((RSAPrivateCrtKey) ca.getCaKeyPair().getPrivate(), caPrivateKey);
		System.out.println("Saving CA public-key to " + caPrivateKey.toAbsolutePath().toString());
		PemIo.writePublicKeyToPem((RSAPublicKey) ca.getCaKeyPair().getPublic(), caPublicKey);

		X500Name serverSubject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, "test-server");
		System.out.println("Generating 4096 bit key pair for server cert");
		KeyPair serverRsaKeyPair = CertificationRequestBuilder.createRsaKeyPair4096Bit();
		JcaPKCS10CertificationRequest serverRequest = CertificationRequestBuilder
				.createServerCertificationRequest(serverSubject, serverRsaKeyPair, null, "localhost");

		System.out.println("Signing server cert");
		X509Certificate serverCertificate = ca.signWebServerCertificate(serverRequest);

		System.out.println("Saving server certificate (p12) to " + serverCertP12.toAbsolutePath().toString());
		CertificateWriter.toPkcs12(serverCertP12, serverRsaKeyPair.getPrivate(), CERT_PASSWORD, serverCertificate,
				caCertificate, "test-server");
		System.out.println("Saving server certificate (pem) to " + serverCertPem.toAbsolutePath().toString());
		PemIo.writeX509CertificateToPem(serverCertificate, serverCertPem);
		System.out.println("Saving server private-key to " + serverPrivateKey.toAbsolutePath().toString());
		PemIo.writePrivateKeyToPem((RSAPrivateCrtKey) serverRsaKeyPair.getPrivate(), serverPrivateKey);
		System.out.println("Saving server public-key to " + serverPublicKey.toAbsolutePath().toString());
		PemIo.writePublicKeyToPem((RSAPublicKey) serverRsaKeyPair.getPublic(), serverPublicKey);

		X500Name clientSubject = CertificationRequestBuilder.createSubject("DE", null, null, null, null, "test-client");
		System.out.println("Generating 4096 bit key pair for client cert");
		KeyPair clientRsaKeyPair = CertificationRequestBuilder.createRsaKeyPair4096Bit();
		JcaPKCS10CertificationRequest clientRequest = CertificationRequestBuilder
				.createClientCertificationRequest(clientSubject, clientRsaKeyPair);

		System.out.println("Signing client cert");
		X509Certificate clientCertificate = ca.signWebClientCertificate(clientRequest);

		System.out.println("Saving client certificate (p21) to " + clientCertP12.toAbsolutePath().toString());
		CertificateWriter.toPkcs12(clientCertP12, clientRsaKeyPair.getPrivate(), CERT_PASSWORD, clientCertificate,
				caCertificate, "test-client");
		System.out.println("Saving client certificate (pem) to " + clientCertPem.toAbsolutePath().toString());
		PemIo.writeX509CertificateToPem(clientCertificate, clientCertPem);
		System.out.println("Saving client private-key to " + clientPrivateKey.toAbsolutePath().toString());
		PemIo.writePrivateKeyToPem((RSAPrivateCrtKey) clientRsaKeyPair.getPrivate(), clientPrivateKey);
		System.out.println("Saving client public-key to " + clientPublicKey.toAbsolutePath().toString());
		PemIo.writePublicKeyToPem((RSAPublicKey) clientRsaKeyPair.getPublic(), clientPublicKey);

		System.out.println("\nAll files except for " + caCertPem.toString() + ", " + serverCertP12.toString() + " and "
				+ clientCertP12.toString() + " can be moved to a different folder.");
		System.out.println("Password for " + serverCertP12.toString() + " and " + clientCertP12.toString() + " is '"
				+ CERT_PASSWORD + "' (without ticks).");
		System.out.println("Add " + clientCertP12.toString()
				+ " to your browsers certificate store to access the server via https. The p12 file includes the Test-CA certificate wich typically gets installed during the p12 import.");
		System.out.println("Add client certificate SHA-512 thumbprint '" + getThumbprintHex(clientCertificate)
				+ "' to the jetty config.properties file.");
	}

	private static boolean exists(String fileType, Path p)
	{
		if (Files.exists(p))
		{
			System.err.println(fileType + " file already exists at " + p.toAbsolutePath().toString());
			return true;
		}
		else
			return false;
	}

	private static String getThumbprintHex(X509Certificate certificate)
	{
		try
		{
			return Hex.encodeHexString(MessageDigest.getInstance("SHA-512").digest(certificate.getEncoded()));
		}
		catch (CertificateEncodingException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
