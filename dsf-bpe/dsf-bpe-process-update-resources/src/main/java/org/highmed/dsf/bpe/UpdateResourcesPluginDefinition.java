package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.UpdateResourcesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResourcesPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public String getJarName()
	{
		return "dsf-bpe-process-update-resources-0.4.0";
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/executeUpdateResources.bpmn", "bpe/requestUpdateResources.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(UpdateResourcesConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aExec = ActivityDefinitionResource.file("fhir/ActivityDefinition/executeUpdateResources-0.3.0.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/requestUpdateResources-0.3.0.xml");
		var c = CodeSystemResource.file("fhir/CodeSystem/update-resources-0.3.0.xml");
		var sExec = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-update-resources-0.3.0.xml");
		var sReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-update-resources-0.3.0.xml");
		var v = ValueSetResource.file("fhir/ValueSet/update-resources-0.3.0.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("executeUpdateResources/0.3.0",
				Arrays.asList(aExec, c, sExec, v), "requestUpdateResources/0.3.0", Arrays.asList(aReq, c, sReq, v));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
