package org.highmed.dsf.fhir.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.parser.IParser;

class ResourceProviderImpl implements ResourceProvider
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceProviderImpl.class);

	private static final String VERSION_PATTERN_STRING1 = "#{version}";
	private static final Pattern VERSION_PATTERN1 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String VERSION_PATTERN_STRING2 = "${version}";
	private static final Pattern VERSION_PATTERN2 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING2));

	private static final String DATE_PATTERN_STRING1 = "#{date}";
	private static final Pattern DATE_PATTERN1 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String DATE_PATTERN_STRING2 = "${date}";
	private static final Pattern DATE_PATTERN2 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING2));
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String PLACEHOLDER_PREFIX_SPRING_ESCAPED = "\\${";
	private static final String PLACEHOLDER_PREFIX = "#{";
	private static final Pattern PLACEHOLDER_PREFIX_PATTERN = Pattern.compile(Pattern.quote(PLACEHOLDER_PREFIX));

	private final Map<String, List<ActivityDefinition>> activityDefinitionsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<CodeSystem>> codeSystemsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<NamingSystem>> namingSystemsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<Questionnaire>> questionnairesByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<StructureDefinition>> structureDefinitionsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<ValueSet>> valueSetsByProcessKeyAndVersion = new HashMap<>();

	private final Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion = new HashMap<>();

	ResourceProviderImpl(Map<String, List<ActivityDefinition>> activityDefinitionsByProcessKeyAndVersion,
			Map<String, List<CodeSystem>> codeSystemsByProcessKeyAndVersion,
			Map<String, List<NamingSystem>> namingSystemsByProcessKeyAndVersion,
			Map<String, List<Questionnaire>> questionnairesByProcessKeyAndVersion,
			Map<String, List<StructureDefinition>> structureDefinitionsByProcessKeyAndVersion,
			Map<String, List<ValueSet>> valueSetsByProcessKeyAndVersion,
			Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion)
	{
		if (activityDefinitionsByProcessKeyAndVersion != null)
			this.activityDefinitionsByProcessKeyAndVersion.putAll(activityDefinitionsByProcessKeyAndVersion);
		if (codeSystemsByProcessKeyAndVersion != null)
			this.codeSystemsByProcessKeyAndVersion.putAll(codeSystemsByProcessKeyAndVersion);
		if (namingSystemsByProcessKeyAndVersion != null)
			this.namingSystemsByProcessKeyAndVersion.putAll(namingSystemsByProcessKeyAndVersion);
		if (questionnairesByProcessKeyAndVersion != null)
			this.questionnairesByProcessKeyAndVersion.putAll(questionnairesByProcessKeyAndVersion);
		if (structureDefinitionsByProcessKeyAndVersion != null)
			this.structureDefinitionsByProcessKeyAndVersion.putAll(structureDefinitionsByProcessKeyAndVersion);
		if (valueSetsByProcessKeyAndVersion != null)
			this.valueSetsByProcessKeyAndVersion.putAll(valueSetsByProcessKeyAndVersion);

		if (dependencyResourcesByProcessKeyAndVersion != null)
			this.dependencyResourcesByProcessKeyAndVersion.putAll(dependencyResourcesByProcessKeyAndVersion);
	}

	@Override
	public Optional<ActivityDefinition> getActivityDefinition(String url, String version)
	{
		return getMetadataResource(url, version, activityDefinitionsByProcessKeyAndVersion, ActivityDefinition.class);
	}

	@Override
	public Optional<CodeSystem> getCodeSystem(String url, String version)
	{
		return getMetadataResource(url, version, codeSystemsByProcessKeyAndVersion, CodeSystem.class);
	}

	@Override
	public Optional<NamingSystem> getNamingSystem(String name)
	{
		Optional<NamingSystem> opt = namingSystemsByProcessKeyAndVersion.values().stream().flatMap(List::stream)
				.filter(s -> s.hasName() && s.getName().equals(name)).findFirst();

		if (opt.isEmpty())
			logger.warn("{} name {} not found", NamingSystem.class.getSimpleName(), name);
		else
			logger.debug("{} name {} found", NamingSystem.class.getSimpleName(), name);

		return opt;
	}

	@Override
	public Optional<Questionnaire> getQuestionnaire(String url, String version)
	{
		return getMetadataResource(url, version, questionnairesByProcessKeyAndVersion, Questionnaire.class);
	}

	@Override
	public Optional<StructureDefinition> getStructureDefinition(String url, String version)
	{
		return getMetadataResource(url, version, structureDefinitionsByProcessKeyAndVersion, StructureDefinition.class);
	}

	@Override
	public Optional<ValueSet> getValueSet(String url, String version)
	{
		return getMetadataResource(url, version, valueSetsByProcessKeyAndVersion, ValueSet.class);
	}

	private <T extends MetadataResource> Optional<T> getMetadataResource(String url, String version,
			Map<String, List<T>> resources, Class<T> type)
	{
		Optional<T> opt = resources.values().stream().flatMap(List::stream)
				.filter(r -> r.hasUrl() && r.getUrl().equals(url) && r.hasVersion() && r.getVersion().equals(version))
				.findFirst();

		if (opt.isEmpty())
			logger.warn("{} url {}, version {} not found", type.getSimpleName(), url, version);
		else
			logger.debug("{} url {}, version {} found", type.getSimpleName(), url, version);

		return opt;
	}

	@Override
	public Optional<MetadataResource> getMetadataResouce(AbstractResource resource)
	{
		if (NamingSystem.class.equals(resource.getType()))
			logger.debug("Get {} resource dependency {}, file {}, name {}", resource.getType().getSimpleName(),
					resource.getDependencyNameAndVersion(), resource.getFileName(), resource.getName());
		else
			logger.debug("Get {} resource dependency {}, file {}, url {}, version {}",
					resource.getType().getSimpleName(), resource.getDependencyNameAndVersion(), resource.getFileName(),
					resource.getUrl(), resource.getVersion());

		if (ActivityDefinition.class.equals(resource.getType()))
			return getActivityDefinition(resource.getUrl(), resource.getVersion()).map(r -> (MetadataResource) r);
		else if (CodeSystem.class.equals(resource.getType()))
			return getCodeSystem(resource.getUrl(), resource.getVersion()).map(r -> (MetadataResource) r);
		else if (NamingSystem.class.equals(resource.getType()))
			return getNamingSystem(resource.getName()).map(r -> (MetadataResource) r);
		else if (Questionnaire.class.equals(resource.getType()))
			return getQuestionnaire(resource.getUrl(), resource.getVersion()).map(r -> (MetadataResource) r);
		else if (StructureDefinition.class.equals(resource.getType()))
			return getStructureDefinition(resource.getUrl(), resource.getVersion()).map(r -> (MetadataResource) r);
		else if (ValueSet.class.equals(resource.getType()))
			return getValueSet(resource.getUrl(), resource.getVersion()).map(r -> (MetadataResource) r);
		else
		{
			logger.warn("Resource of type {} not supported", resource.getType().getSimpleName());
			return Optional.empty();
		}
	}

	@Override
	public Stream<MetadataResource> getResources(String processKeyAndVersion,
			Function<String, ResourceProvider> providerByNameAndVersion)
	{
		List<AbstractResource> list = dependencyResourcesByProcessKeyAndVersion.getOrDefault(processKeyAndVersion,
				Collections.emptyList());

		Stream<MetadataResource> dependencyResources = list.stream()
				.map(r -> providerByNameAndVersion.apply(r.getDependencyNameAndVersion()).getMetadataResouce(r))
				.filter(Optional::isPresent).map(Optional::get);

		Stream<MetadataResource> resources = Arrays.asList(
				activityDefinitionsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
				codeSystemsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
				namingSystemsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
				questionnairesByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
				structureDefinitionsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
				valueSetsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList())).stream()
				.flatMap(List::stream);

		return Stream.concat(resources, dependencyResources);
	}

	static ResourceProvider of(Map<String, List<MetadataResource>> resourcesByProcessKeyAndVersion)
	{
		return of(resourcesByProcessKeyAndVersion, Collections.emptyMap());
	}

	static ResourceProvider of(Map<String, List<MetadataResource>> resourcesByProcessKeyAndVersion,
			Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion)
	{
		Map<String, List<ActivityDefinition>> activityDefinitionsByProcessKeyAndVersion = new HashMap<>();
		Map<String, List<CodeSystem>> codeSystemsByProcessKeyAndVersion = new HashMap<>();
		Map<String, List<NamingSystem>> namingSystemsByProcessKeyAndVersion = new HashMap<>();
		Map<String, List<Questionnaire>> questionnairesByProcessKeyAndVersion = new HashMap<>();
		Map<String, List<StructureDefinition>> structureDefinitionsByProcessKeyAndVersion = new HashMap<>();
		Map<String, List<ValueSet>> valueSetsByProcessKeyAndVersion = new HashMap<>();

		resourcesByProcessKeyAndVersion.entrySet().forEach(e ->
		{
			e.getValue().forEach(r ->
			{
				if (r instanceof ActivityDefinition)
					addOrInsert(activityDefinitionsByProcessKeyAndVersion, e.getKey(), (ActivityDefinition) r);
				else if (r instanceof CodeSystem)
					addOrInsert(codeSystemsByProcessKeyAndVersion, e.getKey(), (CodeSystem) r);
				else if (r instanceof NamingSystem)
					addOrInsert(namingSystemsByProcessKeyAndVersion, e.getKey(), (NamingSystem) r);
				else if (r instanceof Questionnaire)
					addOrInsert(questionnairesByProcessKeyAndVersion, e.getKey(), (Questionnaire) r);
				else if (r instanceof StructureDefinition)
					addOrInsert(structureDefinitionsByProcessKeyAndVersion, e.getKey(), (StructureDefinition) r);
				else if (r instanceof ValueSet)
					addOrInsert(valueSetsByProcessKeyAndVersion, e.getKey(), (ValueSet) r);
				else
					logger.warn("MetadataResource of type {} not supported, ignoring resource",
							r.getResourceType().name());
			});
		});

		return new ResourceProviderImpl(activityDefinitionsByProcessKeyAndVersion, codeSystemsByProcessKeyAndVersion,
				namingSystemsByProcessKeyAndVersion, questionnairesByProcessKeyAndVersion,
				structureDefinitionsByProcessKeyAndVersion, valueSetsByProcessKeyAndVersion,
				dependencyResourcesByProcessKeyAndVersion);
	}

	private static <T extends MetadataResource> void addOrInsert(Map<String, List<T>> map, String processKeyAndVersion,
			T resource)
	{
		if (map.containsKey(processKeyAndVersion))
			map.get(processKeyAndVersion).add(resource);
		else
		{
			List<T> list = new ArrayList<>();
			list.add(resource);
			map.put(processKeyAndVersion, list);
		}
	}

	static ResourceProvider read(String processPluginVersion, LocalDate processPluginDate,
			Supplier<IParser> parserSupplier, ClassLoader classLoader, PropertyResolver resolver,
			Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion)
	{
		Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion = new HashMap<>();
		for (Entry<String, List<AbstractResource>> entry : resourcesByProcessKeyAndVersion.entrySet())
		{
			dependencyResourcesByProcessKeyAndVersion.put(entry.getKey(), entry.getValue().stream()
					.filter(AbstractResource::isDependencyResource).collect(Collectors.toList()));
		}

		Map<String, MetadataResource> resourcesByFileName = new HashMap<>();
		Map<String, List<MetadataResource>> resources = new HashMap<>();
		for (Entry<String, List<AbstractResource>> entry : resourcesByProcessKeyAndVersion.entrySet())
		{
			resources.put(entry.getKey(), entry.getValue().stream()
					.filter(Predicate.not(AbstractResource::isDependencyResource)).map(r -> read(processPluginVersion,
							processPluginDate, parserSupplier, classLoader, resolver, resourcesByFileName, r))
					.collect(Collectors.toList()));
		}

		return of(resources, dependencyResourcesByProcessKeyAndVersion);
	}

	private static MetadataResource read(String processPluginVersion, LocalDate processPluginDate,
			Supplier<IParser> parserSupplier, ClassLoader classLoader, PropertyResolver resolver,
			Map<String, MetadataResource> resourcesByFileName, AbstractResource resources)
	{
		final String fileName = resources.getFileName();
		final Class<? extends MetadataResource> type = resources.getType();

		if (resourcesByFileName.containsKey(fileName))
		{
			MetadataResource m = resourcesByFileName.get(fileName);
			if (type.isInstance(m))
				return m;
			else
				throw new RuntimeException("Unexpected type (" + m.getClass().getSimpleName() + " vs "
						+ type.getSimpleName() + ") for file " + fileName + " while retrieving from cache");
		}
		else
		{
			MetadataResource m = parseResourceAndSetVersion(processPluginVersion, processPluginDate, parserSupplier,
					classLoader, resolver, fileName, type);

			resourcesByFileName.put(fileName, m);

			return m;
		}
	}

	private static <T extends MetadataResource> T parseResourceAndSetVersion(String processPluginVersion,
			LocalDate processPluginDate, Supplier<IParser> parserSupplier, ClassLoader classLoader,
			PropertyResolver resolver, String fileName, Class<T> type)
	{
		logger.debug("Reading {} from {} and replacing all occurrences of {} and {} with {}", type.getSimpleName(),
				fileName, VERSION_PATTERN_STRING1, VERSION_PATTERN_STRING2, processPluginVersion);

		String processPluginDateValue = null;
		if (processPluginDate != null && !LocalDate.MIN.equals(processPluginDate))
		{
			processPluginDateValue = processPluginDate.format(DATE_FORMAT);
			logger.debug("Replacing all occurrences of {} and {} with {}", DATE_PATTERN_STRING1, DATE_PATTERN_STRING2,
					processPluginDateValue);
		}

		try (InputStream in = classLoader.getResourceAsStream(fileName))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);

			read = VERSION_PATTERN1.matcher(read).replaceAll(processPluginVersion);
			read = VERSION_PATTERN2.matcher(read).replaceAll(processPluginVersion);

			if (processPluginDateValue != null)
			{
				read = DATE_PATTERN1.matcher(read).replaceAll(processPluginDateValue);
				read = DATE_PATTERN2.matcher(read).replaceAll(processPluginDateValue);
			}

			// when calling replaceAll with ${ the $ needs to be escaped using \${
			read = PLACEHOLDER_PREFIX_PATTERN.matcher(read).replaceAll(PLACEHOLDER_PREFIX_SPRING_ESCAPED);
			read = resolver.resolveRequiredPlaceholders(read);

			return parserSupplier.get().parseResource(type, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
