package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.PingConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;

import ca.uhn.fhir.context.FhirContext;

public class PingProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public String getJarName()
	{
		return "dsf-bpe-process-ping-0.4.0";
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/ping.bpmn", "bpe/pong.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(PingConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aPing = ActivityDefinitionResource.file("fhir/ActivityDefinition/ping-0.3.0.xml");
		var aPong = ActivityDefinitionResource.file("fhir/ActivityDefinition/pong-0.3.0.xml");
		var tPing = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-pong-0.3.0.xml");
		var tStartPing = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-start-ping-process-0.3.0.xml");
		var tPong = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-ping-0.3.0.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("ping/0.3.0",
				Arrays.asList(aPing, tPong, tStartPing), "pong/0.3.0", Arrays.asList(aPong, tPing));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
