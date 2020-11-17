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
	@Override
	public String getJarName()
	{
		return "dsf-bpe-process-update-allow-list-0.4.0";
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
		var aDown = ActivityDefinitionResource.file("fhir/ActivityDefinition/downloadAllowList-0.3.0.xml");
		var aUp = ActivityDefinitionResource.file("fhir/ActivityDefinition/updateAllowList-0.3.0.xml");
		var c = CodeSystemResource.file("fhir/CodeSystem/update-allow-list-0.3.0.xml");
		var sDown = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-download-allow-list-0.3.0.xml");
		var sUp = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-update-allow-list-0.3.0.xml");
		var v = ValueSetResource.file("fhir/ValueSet/update-allow-list-0.3.0.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("downloadAllowList/0.3.0",
				Arrays.asList(aDown, c, sDown, v), "updateAllowList/0.3.0", Arrays.asList(aUp, c, sUp, v));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
