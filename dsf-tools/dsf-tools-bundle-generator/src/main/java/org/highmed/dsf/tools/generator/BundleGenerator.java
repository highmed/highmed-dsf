package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BundleGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(BundleGenerator.class);

	private static final String BUNDLE_FILENAME = "bundle.xml";

	private final FhirContext fhirContext = FhirContext.forR4();
	private final Path baseFolder;

	public BundleGenerator(Path baseFolder)
	{
		this.baseFolder = baseFolder;
	}

	public Path getBundleFilename()
	{
		return baseFolder.resolve(BUNDLE_FILENAME);
	}

	private IParser newXmlParser()
	{
		IParser parser = fhirContext.newXmlParser();
		parser.setStripVersionsFromReferences(false);
		parser.setOverrideResourceIdWithBundleEntryFullUrl(false);
		parser.setPrettyPrint(true);
		return parser;
	}

	public Bundle generateBundle() throws IOException
	{
		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);

		BundleEntryPutReader putReader = (resource, resourceFile, putFile) ->
		{
			logger.info("Reading {} at {} with put file {}", resource.getSimpleName(), resourceFile.toString(),
					putFile.toString());

			try (InputStream in = Files.newInputStream(resourceFile))
			{
				Resource r = newXmlParser().parseResource(resource, in);
				String putUrl = Files.readString(putFile);

				BundleEntryComponent entry = bundle.addEntry();
				entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
				entry.setResource(r);
				entry.getRequest().setMethod(HTTPVerb.PUT).setUrl(putUrl);
			}
			catch (IOException e)
			{
				logger.error("Error while parsing {} from {}", resource.getSimpleName(), resourceFile.toString());
			}
		};

		FileVisitor<Path> visitor = new BundleEntryFileVisitor(baseFolder, putReader);
		Files.walkFileTree(baseFolder, visitor);

		return bundle;
	}

	private void saveBundle(Bundle bundle) throws IOException
	{
		try (OutputStream out = Files.newOutputStream(getBundleFilename());
				OutputStreamWriter writer = new OutputStreamWriter(out))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
	}

	public static void main(String[] args) throws IOException
	{
		BundleGenerator bundleGenerator = new BundleGenerator(getBaseFolder(args));

		Bundle bundle;
		try
		{
			logger.info("Generating bundle at " + bundleGenerator.getBundleFilename() + " ...");
			bundle = bundleGenerator.generateBundle();
		}
		catch (IOException e)
		{
			logger.error("Error while generating bundle", e);
			throw e;
		}
		try
		{
			bundleGenerator.saveBundle(bundle);
			logger.info("Bundle saved at " + bundleGenerator.getBundleFilename());
		}
		catch (IOException e)
		{
			logger.error("Error while generating bundle", e);
			throw e;
		}
	}

	private static Path getBaseFolder(String[] args)
	{
		if (args.length != 1)
			throw new IllegalArgumentException(
					"Single command-line argument expected, but got " + Arrays.toString(args));

		Path basedFolder = Paths.get(args[0]);

		if (!Files.isReadable(basedFolder))
			throw new IllegalArgumentException("Base folder '" + basedFolder.toString() + "' not readable");

		return basedFolder;
	}
}
