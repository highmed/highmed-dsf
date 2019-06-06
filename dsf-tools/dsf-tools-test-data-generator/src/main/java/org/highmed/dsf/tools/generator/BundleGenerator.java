package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
		return parser;
	}

	public void createIdeTestServerBundle(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Path testBundleTemplateFile = Paths.get("src/main/resources/test-bundle.xml");

		Bundle testBundle = readBundle(testBundleTemplateFile);
		Organization organization = (Organization) testBundle.getEntry().get(0).getResource();
		Extension thumbprintExtension = organization
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint");
		thumbprintExtension.setValue(new StringType(
				clientCertificateFilesByCommonName.get("test-client").getCertificateSha512ThumbprintHex()));

		// TODO
		System.out.println(fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(testBundle));
	}

	public void createDockerServerBundles(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		// TODO Auto-generated method stub

	}

	public void copyIdeTestServerBundle()
	{
		// TODO Auto-generated method stub
		
	}

	public void copyDockerServerBundles()
	{
		// TODO Auto-generated method stub
		
	}
}
