package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.ParentConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;

import ca.uhn.fhir.context.FhirContext;

public class ParentProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public String getJarName()
	{
		return "bpe/dsf-bpe-process-parent-0.4.0";
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("parent.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(ParentConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var sT = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-parent-plugin-0.3.0.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("parentPlugin/1.0.0",
				Arrays.asList(sT));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
