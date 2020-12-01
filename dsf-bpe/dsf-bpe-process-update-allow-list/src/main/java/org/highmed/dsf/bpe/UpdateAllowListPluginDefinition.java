package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.UpdateAllowListConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class UpdateAllowListPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.4.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-update-allow-list";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/updateAllowList.bpmn", "bpe/downloadAllowList.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(UpdateAllowListConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aDown = ActivityDefinitionResource.file("fhir/ActivityDefinition/downloadAllowList.xml");
		var aUp = ActivityDefinitionResource.file("fhir/ActivityDefinition/updateAllowList.xml");
		var c = CodeSystemResource.file("fhir/CodeSystem/update-allow-list.xml");
		var sDown = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-download-allow-list.xml");
		var sUp = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-update-allow-list.xml");
		var v = ValueSetResource.file("fhir/ValueSet/update-allow-list.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("downloadAllowList/" + VERSION,
				Arrays.asList(aDown, c, sDown, v), "updateAllowList/" + VERSION, Arrays.asList(aUp, c, sUp, v));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
