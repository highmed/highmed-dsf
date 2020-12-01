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
	public static final String VERSION = "0.4.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-ping";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
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
		var aPing = ActivityDefinitionResource.file("fhir/ActivityDefinition/ping.xml");
		var aPong = ActivityDefinitionResource.file("fhir/ActivityDefinition/pong.xml");
		var tPing = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-pong.xml");
		var tStartPing = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-start-ping-process.xml");
		var tPong = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-ping.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("ping/" + VERSION,
				Arrays.asList(aPing, tPong, tStartPing), "pong/" + VERSION, Arrays.asList(aPong, tPing));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
