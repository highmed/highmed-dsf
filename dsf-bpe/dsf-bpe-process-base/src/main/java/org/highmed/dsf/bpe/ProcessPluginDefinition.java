package org.highmed.dsf.bpe;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.NamingSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

/**
 * A provider configuration file named "org.highmed.dsf.DsfProcessPluginDefinition" containing the canonical name of the
 * class implementing this interface needs to be part of the process plugin at "/META-INF/services/". For more details
 * on the content of the provider configuration file, see {@link ServiceLoader}.
 *
 * Additional {@link TypedValueSerializer}s to be registered inside the camunda process engine need be defined as beans
 * in the process plugins spring context.
 */
public interface ProcessPluginDefinition
{
	/**
	 * @return process plugin name, same as jar name excluding suffix <code>-&lt;version&gt;.jar</code> used by other
	 *         processes when defining dependencies, e.g. <code>foo-1.2.3</code> for a jar called
	 *         <code>foo-1.2.3.jar</code> or <code>foo-1.2.3-SNAPSHOT.jar</code> with processPluginName <code>foo</code>
	 *         and version <code>1.2.3</code>
	 */
	String getName();

	/**
	 * @return version of the process plugin, processes and fhir resources, e.g. <code>1.2.3</code>
	 */
	String getVersion();

	/**
	 * @return <code>name-version</code>
	 */
	default String getNameAndVersion()
	{
		return getName() + "-" + getVersion();
	}

	/**
	 * <i>Override this method to replace <code>#{date}</code> in FHIR and BPMN files with the returned value.</i>
	 *
	 * @return the release date of the process plugin, if not overridden {@link LocalDate#MIN}
	 * @see ResourceProvider#read(String, LocalDate, java.util.function.Supplier, ClassLoader, PropertyResolver,
	 *      java.util.Map)
	 */
	default LocalDate getReleaseDate()
	{
		return LocalDate.MIN;
	}

	/**
	 * Return <code>Stream.of("foo.bpmn");</code> for a foo.bpmn file located in the root folder of the process plugin
	 * jar. The returned files will be read via {@link ClassLoader#getResourceAsStream(String)}.
	 *
	 * @return *.bpmn files inside process plugin jar
	 *
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	Stream<String> getBpmnFiles();

	/**
	 * @return {@link Configuration} annotated classes defining {@link Bean} annotated factory methods
	 */
	Stream<Class<?>> getSpringConfigClasses();

	/**
	 * <i>Override this method to return a {@link ResourceProvider} with fhir metadata resources (ActivityDefinition,
	 * CodeSystem, NamingSystem, StructureDefinition, ValueSet) needed by this process plugin.</i>
	 *
	 * @param fhirContext
	 *            applications fhir context, never <code>null</code>
	 * @param classLoader
	 *            class loader that was used to initialize the process plugin, never <code>null</code>
	 * @param resolver
	 *            property resolver used to access config properties and to replace place holders in fhir resources,
	 *            never <code>null</code>
	 * @return {@link ResourceProvider} with FHIR resources needed to enable the included processes, if not overridden
	 *         {@link ResourceProvider#empty()}
	 *
	 * @see ActivityDefinitionResource#file(String)
	 * @see CodeSystemResource#file(String)
	 * @see NamingSystemResource#file(String)
	 * @see StructureDefinitionResource#file(String)
	 * @see ValueSetResource#file(String)
	 *
	 * @see FhirContext#newJsonParser()
	 * @see FhirContext#newXmlParser()
	 *
	 * @see ResourceProvider#read(String, java.util.function.Supplier, ClassLoader, PropertyResolver, java.util.Map)
	 * @see ResourceProvider#read(String, LocalDate, java.util.function.Supplier, ClassLoader, PropertyResolver,
	 *      java.util.Map)
	 */
	default ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		return ResourceProvider.empty();
	}

	/**
	 * @return dependencies to other processes by jar name (excluding '.jar'). The system will add ".jar" and
	 *         "-SNAPSHOT.jar" while searching for jars, e.g. "bar-1.2.3"
	 */
	default List<String> getDependencyNamesAndVersions()
	{
		return Collections.emptyList();
	}

	/**
	 * <i>Override this method to implement custom logic after a process has been deployed and is active, e.g. test the
	 * connection to an external server needed by a process.</i>
	 *
	 * @param pluginApplicationContext
	 *            the process plugin spring application context, never <code>null</code>
	 * @param activeProcesses
	 *            active processes from this plugin by process key
	 */
	default void onProcessesDeployed(ApplicationContext pluginApplicationContext, List<String> activeProcesses)
	{
	}
}
