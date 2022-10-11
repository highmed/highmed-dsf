package org.highmed.dsf.fhir.resources;

import java.time.LocalDate;
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
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.parser.IParser;

public interface ResourceProvider
{
	Optional<ActivityDefinition> getActivityDefinition(String url, String version);

	Optional<CodeSystem> getCodeSystem(String url, String version);

	Optional<NamingSystem> getNamingSystem(String name);

	Optional<Questionnaire> getQuestionnaire(String url, String version);

	Optional<StructureDefinition> getStructureDefinition(String url, String version);

	Optional<ValueSet> getValueSet(String url, String version);

	Optional<MetadataResource> getMetadataResouce(AbstractResource resource);

	Stream<MetadataResource> getResources(String processKeyAndVersion,
			Function<String, ResourceProvider> providerByJarName);

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
			public Optional<Questionnaire> getQuestionnaire(String url, String version)
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

	/**
	 * @param processPluginVersion
	 *            version of the process plugin, e.g 1.2.3, used for replacing placeholder #{version}, not
	 *            <code>null</code>
	 * @param parserSupplier
	 *            function to retrieve a {@link IParser} for parsing fhir resources, not <code>null</code>
	 * @param classLoader
	 *            class loader that was used to initialize the process plugin, not <code>null</code>
	 * @param resolver
	 *            property resolver used to access config properties and to replace place holders in fhir resources, not
	 *            <code>null</code>
	 * @param resourcesByProcessKeyAndVersion,
	 *            fhir resources for this process plugin, not <code>null</code>
	 * @return {@link ResourceProvider} for this process plugin
	 */
	static ResourceProvider read(String processPluginVersion, Supplier<IParser> parserSupplier, ClassLoader classLoader,
			PropertyResolver resolver, Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion)
	{
		return read(processPluginVersion, LocalDate.MIN, parserSupplier, classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}

	/**
	 * @param processPluginVersion
	 *            version of the process plugin, e.g <code>"1.2.3"</code>, used for replacing placeholder #{version},
	 *            not <code>null</code>
	 * @param processPluginDate
	 *            release date of the process plugin, e.g <code>LocalDate.of(2021, 2, 23)</code> , used for replacing
	 *            placeholder #{date}, not <code>null</code>
	 * @param parserSupplier
	 *            function to retrieve a {@link IParser} for parsing fhir resources, not <code>null</code>
	 * @param classLoader
	 *            class loader that was used to initialize the process plugin, not <code>null</code>
	 * @param resolver
	 *            property resolver used to access config properties and to replace place holders in fhir resources, not
	 *            <code>null</code>
	 * @param resourcesByProcessKeyAndVersion,
	 *            fhir resources for this process plugin, not <code>null</code>
	 * @return {@link ResourceProvider} for this process plugin
	 */
	static ResourceProvider read(String processPluginVersion, LocalDate processPluginDate,
			Supplier<IParser> parserSupplier, ClassLoader classLoader, PropertyResolver resolver,
			Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion)
	{
		return ResourceProviderImpl.read(processPluginVersion, processPluginDate, parserSupplier, classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
