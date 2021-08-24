package org.highmed.dsf.fhir.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.parser.IParser;

class ResourceProviderImpl implements ResourceProvider
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceProviderImpl.class);

	private static final String VERSION_PATTERN_STRING = "${version}";
	private static final Pattern VERSION_PATTERN = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING));

	private final Map<String, List<ActivityDefinition>> activityDefinitionsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<CodeSystem>> codeSystemsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<NamingSystem>> namingSystemsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<StructureDefinition>> structureDefinitionsByProcessKeyAndVersion = new HashMap<>();
	private final Map<String, List<ValueSet>> valueSetsByProcessKeyAndVersion = new HashMap<>();

	private final Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion = new HashMap<>();

	ResourceProviderImpl(Map<String, List<ActivityDefinition>> activityDefinitionsByProcessKeyAndVersion,
			Map<String, List<CodeSystem>> codeSystemsByProcessKeyAndVersion,
			Map<String, List<NamingSystem>> namingSystemsByProcessKeyAndVersion,
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

		Stream<MetadataResource> resources = Arrays
				.asList(activityDefinitionsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion,
						Collections.emptyList()),
						codeSystemsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
						namingSystemsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()),
						structureDefinitionsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion,
								Collections.emptyList()),
						valueSetsByProcessKeyAndVersion.getOrDefault(processKeyAndVersion, Collections.emptyList()))
				.stream().flatMap(List::stream);

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
				namingSystemsByProcessKeyAndVersion, structureDefinitionsByProcessKeyAndVersion,
				valueSetsByProcessKeyAndVersion, dependencyResourcesByProcessKeyAndVersion);
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

	static ResourceProvider read(String processPluginVersion, Supplier<IParser> parserSupplier, ClassLoader classLoader,
			PropertyResolver resolver, Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion)
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
					.filter(Predicate.not(AbstractResource::isDependencyResource))
					.map(r -> read(processPluginVersion, parserSupplier, classLoader, resolver, resourcesByFileName, r))
					.collect(Collectors.toList()));
		}

		return of(resources, dependencyResourcesByProcessKeyAndVersion);
	}

	private static MetadataResource read(String processPluginVersion, Supplier<IParser> parserSupplier,
			ClassLoader classLoader, PropertyResolver resolver, Map<String, MetadataResource> resourcesByFileName,
			AbstractResource resources)
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
			MetadataResource m = parseResourceAndSetVersion(processPluginVersion, parserSupplier, classLoader, resolver,
					fileName, type);

			resourcesByFileName.put(fileName, m);

			return m;
		}
	}

	private static <T extends MetadataResource> T parseResourceAndSetVersion(String processPluginVersion,
			Supplier<IParser> parserSupplier, ClassLoader classLoader, PropertyResolver resolver, String fileName,
			Class<T> type)
	{
		logger.debug("Reading {} from {} and replacing all occurrence of {} with {}", type.getSimpleName(), fileName,
				VERSION_PATTERN_STRING, processPluginVersion);

		try (InputStream in = classLoader.getResourceAsStream(fileName))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = VERSION_PATTERN.matcher(read).replaceAll(processPluginVersion);

			read = resolver.resolveRequiredPlaceholders(read);

			return parserSupplier.get().parseResource(type, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
