package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.IParser;

public class BundleGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(BundleGenerator.class);

	private static final String BUNDLE_FILENAME = "bundle.xml";
	private static final String DELETE_RESOURCES_FILENAME = "resources.delete";

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

		Path deleteFile = baseFolder.resolve(DELETE_RESOURCES_FILENAME);
		logger.debug("Reading URLs from {} file", deleteFile.toString());

		Files.readAllLines(deleteFile).forEach(url ->
		{
			BundleEntryComponent entry = bundle.addEntry();
			entry.getRequest().setMethod(HTTPVerb.DELETE).setUrl(url);
		});

		BundleEntryPutReader putReader = (resource, resourceFile, putFile) ->
		{
			logger.debug("Reading {} at {} with put file {}", resource.getSimpleName(), resourceFile.toString(),
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
				String ifNoneExistValue = Files.readString(postFile);

				BundleEntryComponent entry = bundle.addEntry();
				entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
				entry.setResource(r);
				entry.getRequest().setMethod(HTTPVerb.POST).setUrl(r.getResourceType().name())
						.setIfNoneExist(ifNoneExistValue);
			}
			catch (IOException e)
			{
				logger.error("Error while parsing {} from {}", resource.getSimpleName(), resourceFile.toString());
			}
		};

		FileVisitor<Path> visitor = new BundleEntryFileVisitor(baseFolder, putReader, postReader);
		Files.walkFileTree(baseFolder, visitor);

		sortBundleEntries(bundle);

		return bundle;
	}

	private void sortBundleEntries(Bundle bundle)
	{
		List<StructureDefinition> definitions = bundle.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.map(BundleEntryComponent::getResource).filter(r -> r instanceof StructureDefinition)
				.map(sd -> (StructureDefinition) sd).collect(Collectors.toList());

		List<String> urlsSortedByDependencies = getUrlsSortedByDependencies(definitions);

		List<BundleEntryComponent> sortedEntries = bundle.getEntry().stream()
				.sorted(Comparator.comparingInt(getSortCriteria1())
						.thenComparing(Comparator.comparingInt(getSortCriteria2(urlsSortedByDependencies)))
						.thenComparing(Comparator.comparing(getSortCriteria3())))
				.collect(Collectors.toList());
		bundle.setEntry(sortedEntries);
	}

	private List<String> getUrlsSortedByDependencies(List<StructureDefinition> definitions)
	{
		Map<String, List<String>> dependencies = definitions.stream()
				.collect(Collectors.toMap(StructureDefinition::getUrl,
						d -> d.getDifferential().getElement().stream().filter(ElementDefinition::hasType)
								.map(ElementDefinition::getType).flatMap(List::stream)
								.filter(TypeRefComponent::hasProfile).map(TypeRefComponent::getProfile)
								.flatMap(List::stream).map(CanonicalType::getValue).collect(Collectors.toList())));

		List<String> handled = new ArrayList<>();

		return dependencies.keySet().stream().sorted().flatMap(url ->
		{
			if (handled.contains(url))
				return Stream.empty();
			else
			{
				handled.add(url);
				return getSorted(dependencies, url, handled);
			}
		}).collect(Collectors.toList());
	}

	private Stream<String> getSorted(Map<String, List<String>> allDependencies, String current, List<String> handled)
	{
		List<String> dependencies = allDependencies.get(current);
		if (dependencies.isEmpty())
		{
			handled.add(current);
			return Stream.of(current);
		}
		else
			return Stream.concat(dependencies.stream().flatMap(c -> getSorted(allDependencies, c, handled)),
					Stream.of(current));
	}

	private ToIntFunction<BundleEntryComponent> getSortCriteria1()
	{
		return (BundleEntryComponent e) ->
		{
			if (e.getResource() == null)
				return Integer.MIN_VALUE;
			else
				switch (e.getResource().getClass().getAnnotation(ResourceDef.class).name())
				{
					case "CodeSystem":
						return 1;
					case "NamingSystem":
						return 2;
					case "ValueSet":
						return 3;
					case "StructureDefinition":
						return 4;
					case "Subscription":
						return 5;

					default:
						return Integer.MAX_VALUE;
				}
		};
	}

	private ToIntFunction<BundleEntryComponent> getSortCriteria2(List<String> urlsSortedByDependencies)
	{
		return (BundleEntryComponent e) ->
		{
			if (e.getResource() == null || !(e.getResource() instanceof StructureDefinition))
				return -1;
			else
				return urlsSortedByDependencies.indexOf(((StructureDefinition) e.getResource()).getUrl());
		};
	}

	private Function<BundleEntryComponent, String> getSortCriteria3()
	{
		return (BundleEntryComponent e) ->
		{

			if (e.getResource() == null)
				return "";
			else if (e.getResource() instanceof CodeSystem)
			{
				CodeSystem cs = (CodeSystem) e.getResource();
				return cs.getUrl() + "|" + cs.getVersion();
			}
			else if (e.getResource() instanceof NamingSystem)
			{
				NamingSystem ns = (NamingSystem) e.getResource();
				return ns.getName();
			}
			else if (e.getResource() instanceof ValueSet)
			{
				ValueSet vs = (ValueSet) e.getResource();
				return vs.getUrl() + "|" + vs.getVersion();
			}
			else if (e.getResource() instanceof StructureDefinition)
			{
				StructureDefinition sd = (StructureDefinition) e.getResource();
				return sd.getUrl() + "|" + sd.getVersion();
			}
			else if (e.getResource() instanceof Subscription)
			{
				Subscription s = (Subscription) e.getResource();
				return s.getReason();
			}
			else
				return "";
		};
	}

	private void saveBundle(Bundle bundle) throws IOException
	{
		try (OutputStream out = Files.newOutputStream(getBundleFilename());
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8))
		{
			newXmlParser().encodeResourceToWriter(bundle, writer);
		}
	}

	private void generateStructureDefinitionSnapshots(Bundle bundle, IValidationSupport validationSupport)
	{
		SnapshotGenerator generator = new SnapshotGenerator(fhirContext, validationSupport);

		bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof StructureDefinition)
				.map(r -> (StructureDefinition) r).sorted(Comparator.comparing(StructureDefinition::getUrl).reversed())
				.filter(s -> !s.hasSnapshot()).forEach(s -> generator.generateSnapshot(s));
	}

	private void expandValueSets(Bundle bundle, ValidationSupportChain validationSupport)
	{
		ValueSetExpander valueSetExpander = new ValueSetExpander(fhirContext, validationSupport);

		bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> r instanceof ValueSet).map(r -> (ValueSet) r)
				.filter(v -> !v.hasExpansion()).forEach(v -> valueSetExpander.expand(v));
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
