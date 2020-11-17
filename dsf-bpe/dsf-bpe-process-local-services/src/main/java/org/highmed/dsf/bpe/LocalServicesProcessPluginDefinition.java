package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.LocalServicesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class LocalServicesProcessPluginDefinition implements ProcessPluginDefinition
{
	private static final String DEP_FEASIBILITY = "dsf-bpe-process-feasibility-0.4.0";

	@Override
	public String getJarName()
	{
		return "dsf-bpe-process-local-services-0.4.0";
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/localServicesIntegration.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(LocalServicesConfig.class);
	}

	@Override
	public List<String> getDependencyJarNames()
	{
		return Arrays.asList(DEP_FEASIBILITY);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aL = ActivityDefinitionResource.file("fhir/ActivityDefinition/localServicesIntegration-0.3.0.xml");
		var sTL = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-local-services-integration-0.3.0.xml");
		var sExtG = StructureDefinitionResource.dependency(DEP_FEASIBILITY,
				"http://highmed.org/fhir/StructureDefinition/group-id", "0.3.0");
		var sExtQ = StructureDefinitionResource.dependency(DEP_FEASIBILITY,
				"http://highmed.org/fhir/StructureDefinition/query", "0.3.0");

		var vF = ValueSetResource.dependency(DEP_FEASIBILITY, "http://highmed.org/fhir/ValueSet/feasibility", "0.3.0");
		var vQt = ValueSetResource.dependency(DEP_FEASIBILITY, "http://highmed.org/fhir/ValueSet/query-type", "0.3.0");
		var cF = CodeSystemResource.dependency(DEP_FEASIBILITY, "http://highmed.org/fhir/CodeSystem/feasibility",
				"0.3.0");
		var cQt = CodeSystemResource.dependency(DEP_FEASIBILITY, "http://highmed.org/fhir/CodeSystem/query-type",
				"0.3.0");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("localServicesIntegration/0.3.0",
				Arrays.asList(aL, sTL, vF, cF, vQt, cQt, sExtG, sExtQ));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
