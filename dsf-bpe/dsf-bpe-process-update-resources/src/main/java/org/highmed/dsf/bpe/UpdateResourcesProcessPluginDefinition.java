package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.UpdateResourcesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResourcesProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.4.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-update-resources";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
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
		var aExec = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeUpdateResources.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestUpdateResources.xml");
		var c = CodeSystemResource.file("fhir/CodeSystem/highmed-update-resources.xml");
		var sExec = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-update-resources.xml");
		var sReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-update-resources.xml");
		var v = ValueSetResource.file("fhir/ValueSet/highmed-update-resources.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map
				.of("executeUpdateResources/" + VERSION, Arrays.asList(aExec, c, sExec, v),
						"requestUpdateResources/" + VERSION, Arrays.asList(aReq, c, sReq, v));

		return ResourceProvider
				.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader,
						resourcesByProcessKeyAndVersion);
	}
}
