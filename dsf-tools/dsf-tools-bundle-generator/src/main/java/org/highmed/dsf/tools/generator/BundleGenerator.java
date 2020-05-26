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

import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
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

		BundleEntryPostReader postReader = (resource, resourceFile, postFile) ->
		{
			logger.info("Reading {} at {} with post file {}", resource.getSimpleName(), resourceFile.toString(),
					postFile.toString());

			try (InputStream in = Files.newInputStream(resourceFile))
			{
				Resource r = newXmlParser().parseResource(resource, in);
				String idNoneExistValue = Files.readString(postFile);

				BundleEntryComponent entry = bundle.addEntry();
				entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
				entry.setResource(r);
				entry.getRequest().setMethod(HTTPVerb.POST).setUrl(r.getResourceType().name())
						.setIfNoneExist(idNoneExistValue);
			}
			catch (IOException e)
			{
				logger.error("Error while parsing {} from {}", resource.getSimpleName(), resourceFile.toString());
			}
		};

		FileVisitor<Path> visitor = new BundleEntryFileVisitor(baseFolder, putReader, postReader);
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

	private void generateStructureDefinitionSnapshots(Bundle bundle, IValidationSupport validationSupport)
	{
		SnapshotGenerator generator = new SnapshotGenerator(fhirContext, validationSupport);

		bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof StructureDefinition)
				.map(r -> (StructureDefinition) r).filter(s -> !s.hasSnapshot()).forEach(s ->
				{
					generator.generateSnapshot(s);
					System.out.println("Snapshot generated: " + s.getUrl() + "|" + s.getVersion());
				});
	}

	private void expandValueSets(Bundle bundle, ValidationSupportChain validationSupport)
	{
		ValueSetExpander valueSetExpander = new ValueSetExpander(fhirContext, validationSupport);

		bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof ValueSet).map(r -> (ValueSet) r)
				.filter(s -> !s.hasExpansion()).forEach(v ->
				{
					valueSetExpander.expand(v);
					System.out.println("Expansion generated: " + v.getUrl() + "|" + v.getVersion());
				});
	}

	public static void main(String[] args) throws Exception
	{
		try
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

			ValidationSupportChain validationSupport = new ValidationSupportChain(
					new InMemoryTerminologyServerValidationSupport(bundleGenerator.fhirContext),
					new ValidationSupportWithCustomResources(bundleGenerator.fhirContext, bundle),
					new DefaultProfileValidationSupport(bundleGenerator.fhirContext));

			bundleGenerator.expandValueSets(bundle, validationSupport);
			bundleGenerator.generateStructureDefinitionSnapshots(bundle, validationSupport);

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
		catch (Exception e)
		{
			e.printStackTrace();
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
