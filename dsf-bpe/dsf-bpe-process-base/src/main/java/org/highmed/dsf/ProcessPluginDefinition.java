package org.highmed.dsf;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	 * @return jar name used by other processes when defining dependencies
	 */
	String getJarName();

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
	 * @return @{@link Configuration} annotated classes defining @{@link Bean} annotated factory methods
	 */
	Stream<Class<?>> getSpringConfigClasses();

	/**
	 * @param fhirContext
	 *            the applications fhir context, never <code>null</code>
	 * @param classLoader
	 *            the classLoader that was used to initialize the process plugin, never <code>null</code>
	 * @return {@link ResourceProvider} with FHIR resources needed to enable the included processes
	 */
	default ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		return ResourceProvider.empty();
	}

	/**
	 * @return dependencies to other processes by jar name (excluding '.jar'). The system will add ".jar" and
	 *         "-SNAPSHOT.jar" while searching for jars
	 */
	default List<String> getDependencyJarNames()
	{
		return Collections.emptyList();
	}
}
