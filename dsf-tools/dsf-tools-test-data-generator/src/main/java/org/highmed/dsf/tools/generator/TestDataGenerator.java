package org.highmed.dsf.tools.generator;

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

		CertificateFiles webbrowserTestUser = certificateGenerator.getClientCertificateFilesByCommonName()
				.get("Webbrowser Test User");
		logger.warn(
				"Install client-certificate and CA certificate from \"{}\" into your browsers certificate store to access fhir and bpe servers with your webbrowser",
				webbrowserTestUser.getP12KeyStoreFile().toAbsolutePath().toString());

		bundleGenerator.createTestBundle(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyJavaTestBundle();
		bundleGenerator.copyDockerTestBundle();

		bundleGenerator.createDockerTest3MedicTtpBundles(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyDockerTest3MedicTtpBundles();

		configGenerator
				.modifyJavaTestFhirConfigProperties(certificateGenerator.getClientCertificateFilesByCommonName());
		configGenerator.copyJavaTestFhirConfigProperties();

		configGenerator
				.modifyDockerTestFhirConfigProperties(certificateGenerator.getClientCertificateFilesByCommonName());
		configGenerator.copyDockerTestFhirConfigProperties();

		configGenerator.modifyDockerTest3MedicTtpFhirConfigProperties(
				certificateGenerator.getClientCertificateFilesByCommonName());
		configGenerator.copyDockerTest3MedicTtpFhirConfigProperties();
	}
}
