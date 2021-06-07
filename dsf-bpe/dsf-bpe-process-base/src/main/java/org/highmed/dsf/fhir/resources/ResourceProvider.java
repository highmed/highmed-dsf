package org.highmed.dsf.fhir.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.parser.IParser;

public interface ResourceProvider
{
	Optional<ActivityDefinition> getActivityDefinition(String url, String version);

	Optional<CodeSystem> getCodeSystem(String url, String version);

	Optional<NamingSystem> getNamingSystem(String name);

	Optional<StructureDefinition> getStructureDefinition(String url, String version);

	Optional<ValueSet> getValueSet(String url, String version);

	Optional<MetadataResource> getMetadataResouce(AbstractResource resource);

	Stream<MetadataResource> getResources(String processKeyAndVersion,
			Function<String, ResourceProvider> providerByJarName);

	boolean isEmpty();

	static ResourceProvider empty()
	{
		return new ResourceProvider()
		{
			@Override
			public Optional<ActivityDefinition> getActivityDefinition(String url, String version)
			{
				return Optional.empty();
			}

			@Override
			public Optional<CodeSystem> getCodeSystem(String url, String version)
			{
				return Optional.empty();
			}

			@Override
			public Optional<NamingSystem> getNamingSystem(String name)
			{
				return Optional.empty();
			}

			@Override
			public Optional<StructureDefinition> getStructureDefinition(String url, String version)
			{
				return Optional.empty();
			}

			@Override
			public Optional<ValueSet> getValueSet(String url, String version)
			{
				return Optional.empty();
			}

			@Override
			public Optional<MetadataResource> getMetadataResouce(AbstractResource resource)
			{
				return Optional.empty();
			}

			@Override
			public Stream<MetadataResource> getResources(String processKeyAndVersion,
					Function<String, ResourceProvider> providerByJarName)
			{
				return Stream.empty();
			}

			@Override
			public boolean isEmpty()
			{
				return true;
			}
		};
	}

	static ResourceProvider of(Map<String, List<MetadataResource>> resourcesByProcessKeyAndVersion)
	{
		return ResourceProviderImpl.of(resourcesByProcessKeyAndVersion);
	}

	static ResourceProvider of(Map<String, List<MetadataResource>> resourcesByProcessKeyAndVersion,
			Map<String, List<AbstractResource>> dependencyResourcesByProcessKeyAndVersion)
	{
		return ResourceProviderImpl.of(resourcesByProcessKeyAndVersion, dependencyResourcesByProcessKeyAndVersion);
	}

	static ResourceProvider read(String processPluginVersion, Supplier<IParser> parserSupplier, ClassLoader classLoader,
			PropertyResolver resolver, Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion)
	{
		return ResourceProviderImpl.read(processPluginVersion, parserSupplier, classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
