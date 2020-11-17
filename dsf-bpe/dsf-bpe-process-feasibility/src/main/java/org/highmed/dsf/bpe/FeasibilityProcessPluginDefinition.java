package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.FeasibilityConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.NamingSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public String getJarName()
	{
		return "dsf-bpe-process-feasibility-0.4.0";
	}
	
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/requestSimpleFeasibility.bpmn", "bpe/computeSimpleFeasibility.bpmn",
				"bpe/executeSimpleFeasibility.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(FeasibilityConfig.class, FeasibilitySerializerConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/computeSimpleFeasibility-0.3.0.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/executeSimpleFeasibility-0.3.0.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/requestSimpleFeasibility-0.3.0.xml");

		var cF = CodeSystemResource.file("fhir/CodeSystem/feasibility-0.3.0.xml");
		var cQT = CodeSystemResource.file("fhir/CodeSystem/query-type-0.3.0.xml");

		var n = NamingSystemResource.file("fhir/NamingSystem/highmed-research-study.xml");

		var sExtG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-group-id-0.3.0.xml");
		var sExtPartMeDic = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-medic-0.3.0.xml");
		var sExtPartTtp = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-ttp-0.3.0.xml");
		var sExtQ = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-query-0.3.0.xml");
		var sG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-group-0.3.0.xml");
		var sR = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-research-study-feasibility-0.3.0.xml");
		var sTCom = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-compute-simple-feasibility-0.3.0.xml");
		var sTErr = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-error-simple-feasibility-0.3.0.xml");
		var sTExe = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-simple-feasibility-0.3.0.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-simple-feasibility-0.3.0.xml");
		var sTReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-simple-feasibility-0.3.0.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-simple-feasibility-0.3.0.xml");

		var vF = ValueSetResource.file("fhir/ValueSet/feasibility-0.3.0.xml");
		var vQT = ValueSetResource.file("fhir/ValueSet/query-type-0.3.0.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("computeSimpleFeasibility/0.3.0",
				Arrays.asList(aCom, sTCom, vF, cF, sTResS, sExtG, sG), "executeSimpleFeasibility/0.3.0",
				Arrays.asList(aExe, sTExe, vF, cF, sR, sExtPartTtp, sExtPartMeDic, n, sG, sExtQ, vQT, cQT),
				"requestSimpleFeasibility/0.3.0", Arrays.asList(aReq, sTReq, vF, cF, sR, sExtPartTtp, sExtPartMeDic, n,
						sG, sExtQ, vQT, cQT, sTResM, sExtG, sTErr));

		return ResourceProvider.read(() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
