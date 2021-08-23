package org.highmed.dsf.tools.generator;

import java.nio.file.Path;
import java.util.Map;

import org.highmed.dsf.tools.generator.CertificateGenerator.CertificateFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rwh.utils.crypto.CertificateAuthority;

public class TestDataGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);

	private static final CertificateGenerator certificateGenerator = new CertificateGenerator();
	private static final BundleGenerator bundleGenerator = new BundleGenerator();
	private static final ConfigGenerator configGenerator = new ConfigGenerator();
	private static final EnvGenerator envGenerator = new EnvGenerator();

	static
	{
		CertificateAuthority.registerBouncyCastleProvider();
	}

	public static void main(String[] args)
	{
		certificateGenerator.generateCertificates();

		certificateGenerator.copyJavaTestCertificates();
		certificateGenerator.copyDockerTestCertificates();
		certificateGenerator.copyDockerTest3MedicTtpCertificates();
		certificateGenerator.copyDockerTest3MedicTtpDockerCertificates();

		Map<String, CertificateFiles> clientCertificateFilesByCommonName = certificateGenerator
				.getClientCertificateFilesByCommonName();

		CertificateFiles webbrowserTestUser = clientCertificateFilesByCommonName.get("Webbrowser Test User");
		Path p12File = certificateGenerator.createP12(webbrowserTestUser);
		logger.warn(
				"Install client-certificate and CA certificate from \"{}\" into your browsers certificate store to access fhir and bpe servers with your webbrowser",
				p12File.toAbsolutePath().toString());

		// fhir bundle.xml
		bundleGenerator.createTestBundle(clientCertificateFilesByCommonName);
		bundleGenerator.copyJavaTestBundle();

		// fhir config.properties
		configGenerator.modifyJavaTestFhirConfigProperties(clientCertificateFilesByCommonName);
		configGenerator.copyJavaTestFhirConfigProperties();

		// fhir .env
		envGenerator.generateAndWriteDockerTestFhirEnvFile(clientCertificateFilesByCommonName);
		envGenerator.generateAndWriteDockerTest3MedicTtpFhirEnvFiles(clientCertificateFilesByCommonName);
		envGenerator.generateAndWriteDockerTest3MedicTtpDockerFhirEnvFiles(clientCertificateFilesByCommonName);
	}
}
