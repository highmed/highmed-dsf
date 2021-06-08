package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.dsf.tools.generator.CertificateGenerator.CertificateFiles;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BundleGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(BundleGenerator.class);

	private final FhirContext fhirContext = FhirContext.forR4();
	private final ReferenceExtractor extractor = new ReferenceExtractorImpl();
	private final ReferenceCleaner cleaner = new ReferenceCleanerImpl(extractor);

	private Bundle testBundle;
	private Bundle medic1Bundle;
	private Bundle medic2Bundle;
	private Bundle medic3Bundle;
	private Bundle ttpBundle;
	private Bundle medic1DockerBundle;
	private Bundle medic2DockerBundle;
	private Bundle medic3DockerBundle;
	private Bundle ttpDockerBundle;

	private Bundle readAndCleanBundle(Path bundleTemplateFile)
	{
		try (InputStream in = Files.newInputStream(bundleTemplateFile))
		{
			Bundle bundle = newXmlParser().parseResource(Bundle.class, in);

			// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
			return cleaner.cleanReferenceResourcesIfBundle(bundle);
		}
		catch (IOException e)
		{
			logger.error("Error while reading bundle from " + bundleTemplateFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private void writeBundle(Path bundleFile, Bundle bundle)
	{
		try (OutputStream out = Files.newOutputStream(bundleFile);
				OutputStreamWriter writer = new OutputStreamWriter(out))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
		catch (IOException e)
		{
			logger.error("Error while writing bundle to " + bundleFile.toString(), e);
			throw new RuntimeException(e);
		}
	}

	private IParser newXmlParser()
	{
		IParser parser = fhirContext.newXmlParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	public void createTestBundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Path testBundleTemplateFile = Paths.get("src/main/resources/bundle-templates/test-bundle.xml");

		testBundle = readAndCleanBundle(testBundleTemplateFile);

		Organization organization = (Organization) testBundle.getEntry().get(0).getResource();
		Extension thumbprintExtension = organization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("test-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle/test-bundle.xml"), testBundle);
	}

	public void copyJavaTestBundle()
	{
		Path javaTestBundleFile = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", javaTestBundleFile);
		writeBundle(javaTestBundleFile, testBundle);
	}

	public void copyDockerTestBundle()
	{
		Path dockerTestBundleFile = Paths.get("../../dsf-docker-test-setup/fhir/app/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", dockerTestBundleFile);
		writeBundle(dockerTestBundleFile, testBundle);
	}

	public void createDockerTest3MedicTtpBundles(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		medic1Bundle = createDockerTestMedic1Bundle(clientCertificateFilesByCommonName, "medic1-bundle.xml");
		medic2Bundle = createDockerTestMedic2Bundle(clientCertificateFilesByCommonName, "medic2-bundle.xml");
		medic3Bundle = createDockerTestMedic3Bundle(clientCertificateFilesByCommonName, "medic3-bundle.xml");
		ttpBundle = createDockerTestTtpBundle(clientCertificateFilesByCommonName, "ttp-bundle.xml");

		medic1DockerBundle = createDockerTestMedic1Bundle(clientCertificateFilesByCommonName,
				"medic1-docker-bundle.xml");
		medic2DockerBundle = createDockerTestMedic2Bundle(clientCertificateFilesByCommonName,
				"medic2-docker-bundle.xml");
		medic3DockerBundle = createDockerTestMedic3Bundle(clientCertificateFilesByCommonName,
				"medic3-docker-bundle.xml");
		ttpDockerBundle = createDockerTestTtpBundle(clientCertificateFilesByCommonName, "ttp-docker-bundle.xml");
	}

	private Bundle createDockerTestMedic1Bundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String sourceBundleFileName)
	{
		Path medic1BundleTemplateFile = Paths.get("src/main/resources/bundle-templates", sourceBundleFileName);

		Bundle medic1Bundle = readAndCleanBundle(medic1BundleTemplateFile);

		Organization organizationTtp = (Organization) medic1Bundle.getEntry().get(0).getResource();
		Extension organizationTtpThumbprintExtension = organizationTtp
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationTtpThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("ttp-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic1 = (Organization) medic1Bundle.getEntry().get(1).getResource();
		Extension organizationMedic1thumbprintExtension = organizationMedic1
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic1thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic1-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle", sourceBundleFileName), medic1Bundle);

		return medic1Bundle;
	}

	private Bundle createDockerTestMedic2Bundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String sourceBundleFileName)
	{
		Path medic2BundleTemplateFile = Paths.get("src/main/resources/bundle-templates", sourceBundleFileName);

		Bundle medic2Bundle = readAndCleanBundle(medic2BundleTemplateFile);

		Organization organizationTtp = (Organization) medic2Bundle.getEntry().get(0).getResource();
		Extension organizationTtpThumbprintExtension = organizationTtp
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationTtpThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("ttp-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic2 = (Organization) medic2Bundle.getEntry().get(1).getResource();
		Extension organizationMedic2thumbprintExtension = organizationMedic2
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic2thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic2-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle", sourceBundleFileName), medic2Bundle);

		return medic2Bundle;
	}

	private Bundle createDockerTestMedic3Bundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String sourceBundleFileName)
	{
		Path medic3BundleTemplateFile = Paths.get("src/main/resources/bundle-templates", sourceBundleFileName);

		Bundle medic3Bundle = readAndCleanBundle(medic3BundleTemplateFile);

		Organization organizationTtp = (Organization) medic3Bundle.getEntry().get(0).getResource();
		Extension organizationTtpThumbprintExtension = organizationTtp
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationTtpThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("ttp-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic3 = (Organization) medic3Bundle.getEntry().get(1).getResource();
		Extension organizationMedic3thumbprintExtension = organizationMedic3
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic3thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic3-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle", sourceBundleFileName), medic3Bundle);

		return medic3Bundle;
	}

	private Bundle createDockerTestTtpBundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String sourceBundleFileName)
	{
		Path medic3BundleTemplateFile = Paths.get("src/main/resources/bundle-templates", sourceBundleFileName);

		Bundle ttpBundle = readAndCleanBundle(medic3BundleTemplateFile);

		Organization organizationTtp = (Organization) ttpBundle.getEntry().get(0).getResource();
		Extension organizationTtpThumbprintExtension = organizationTtp
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationTtpThumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("ttp-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic1 = (Organization) ttpBundle.getEntry().get(1).getResource();
		Extension organizationMedic1thumbprintExtension = organizationMedic1
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic1thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic1-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic2 = (Organization) ttpBundle.getEntry().get(2).getResource();
		Extension organizationMedic2thumbprintExtension = organizationMedic2
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic2thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic2-client").getCertificateSha512ThumbprintHex()));

		Organization organizationMedic3 = (Organization) ttpBundle.getEntry().get(3).getResource();
		Extension organizationMedic3thumbprintExtension = organizationMedic3
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		organizationMedic3thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("medic3-client").getCertificateSha512ThumbprintHex()));

		writeBundle(Paths.get("bundle", sourceBundleFileName), ttpBundle);

		return ttpBundle;
	}

	public void copyDockerTest3MedicTtpBundles()
	{
		Path medic1BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic1/fhir/app/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic1BundleFile);
		writeBundle(medic1BundleFile, medic1Bundle);

		Path medic2BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic2/fhir/app/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic2BundleFile);
		writeBundle(medic2BundleFile, medic2Bundle);

		Path medic3BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic3/fhir/app/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic3BundleFile);
		writeBundle(medic3BundleFile, medic3Bundle);

		Path ttpBundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp/ttp/fhir/app/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", ttpBundleFile);
		writeBundle(ttpBundleFile, ttpBundle);
	}

	public void copyDockerTest3MedicTtpDockerBundles()
	{
		Path medic1BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker/medic1/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic1BundleFile);
		writeBundle(medic1BundleFile, medic1DockerBundle);

		Path medic2BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker/medic2/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic2BundleFile);
		writeBundle(medic2BundleFile, medic2DockerBundle);

		Path medic3BundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker/medic3/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", medic3BundleFile);
		writeBundle(medic3BundleFile, medic3DockerBundle);

		Path ttpBundleFile = Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker/ttp/fhir/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", ttpBundleFile);
		writeBundle(ttpBundleFile, ttpDockerBundle);
	}
}
