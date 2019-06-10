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

		CertificateFiles webbrowserTestUser = certificateGenerator.getClientCertificateFilesByCommonName()
				.get("Webbrowser Test User");
		logger.warn(
				"Install client-certificate and CA certificate from \"{}\" into your browsers certificate store to access fhir and bpe servers with your webbrowser",
				webbrowserTestUser.getP12KeyStoreFile().toAbsolutePath().toString());

		bundleGenerator.createJavaTestServerBundle(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyJavaTestServerBundle();

		bundleGenerator.createDockerServerBundles(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyDockerServerBundles();

		configGenerator
				.modifyJavaTestServerConfigProperties(certificateGenerator.getClientCertificateFilesByCommonName());
		configGenerator.copyJavaTestFhirServerConfigProperties();

		configGenerator
				.modifyDockerTestServerConfigProperties(certificateGenerator.getClientCertificateFilesByCommonName());
		configGenerator.copyDockerTestFhirServerConfigProperties();
	}
}
