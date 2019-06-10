package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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

	private Bundle testBundle;

	private Bundle readBundle(Path bundleTemplateFile)
	{
		try (InputStream in = Files.newInputStream(bundleTemplateFile))
		{
			return newXmlParser().parseResource(Bundle.class, in);
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

	public void createJavaTestServerBundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Path testBundleTemplateFile = Paths.get("src/main/resources/bundle-templates/test-bundle.xml");

		testBundle = readBundle(testBundleTemplateFile);
		Organization organization = (Organization) testBundle.getEntry().get(0).getResource();
		Extension thumbprintExtension = organization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint");
		thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("test-client").getCertificateSha512ThumbprintHex()));

		removeReferenceEmbeddedResources(testBundle);

		writeBundle(Paths.get("bundle/test-bundle.xml"), testBundle);
	}

	// FIXME hapi parser can't handle embedded resources and creates them while parsing bundles
	private void removeReferenceEmbeddedResources(Bundle bundle)
	{
		bundle.getEntry().stream().map(e -> e.getResource()).forEach(res ->
		{
			logger.debug("Extracting references from {} resource", res.getResourceType());
			extractor.getReferences(res).forEach(ref ->
			{
				logger.debug("Setting reference embedded resource to null at {}", ref.getReferenceLocation());
				ref.getReference().setResource(null);
			});
		});
	}

	public void copyJavaTestServerBundle()
	{
		Path testBundleFile = Paths.get("../../dsf-fhir/dsf-fhir-server-jetty/conf/bundle.xml");
		logger.info("Copying fhir bundle to {}", testBundleFile);
		writeBundle(testBundleFile, testBundle);
	}

	public void createDockerServerBundles(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		// TODO Auto-generated method stub

	}

	public void copyDockerServerBundles()
	{
		// TODO Auto-generated method stub

	}
}
