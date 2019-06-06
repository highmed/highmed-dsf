package org.highmed.dsf.tools.generator;

import de.rwh.utils.crypto.CertificateAuthority;

public class TestDataGenerator
{
	private static final CertificateGenerator certificateGenerator = new CertificateGenerator();
	private static final BundleGenerator bundleGenerator = new BundleGenerator();

	static
	{
		CertificateAuthority.registerBouncyCastleProvider();
	}

	public static void main(String[] args)
	{
		certificateGenerator.generateCertificates();
		certificateGenerator.copyCertificates();

		bundleGenerator.createIdeTestServerBundle(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyIdeTestServerBundle();
		
		bundleGenerator.createDockerServerBundles(certificateGenerator.getClientCertificateFilesByCommonName());
		bundleGenerator.copyDockerServerBundles();
	}
}
