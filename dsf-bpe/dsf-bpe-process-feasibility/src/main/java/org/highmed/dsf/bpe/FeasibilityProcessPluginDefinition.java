package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
	public static final String VERSION = "0.4.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-feasibility";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
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
		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/computeSimpleFeasibility.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/executeSimpleFeasibility.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/requestSimpleFeasibility.xml");

		var cF = CodeSystemResource.file("fhir/CodeSystem/feasibility.xml");
		var cQT = CodeSystemResource.file("fhir/CodeSystem/query-type.xml");

		var n = NamingSystemResource.file("fhir/NamingSystem/highmed-research-study.xml");

		var sExtG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-group-id.xml");
		var sExtPartMeDic = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-medic.xml");
		var sExtPartTtp = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-ttp.xml");
		var sExtQ = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-query.xml");
		var sG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-group.xml");
		var sR = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-research-study-feasibility.xml");
		var sTCom = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-compute-simple-feasibility.xml");
		var sTErr = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-error-simple-feasibility.xml");
		var sTExe = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-simple-feasibility.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-simple-feasibility.xml");
		var sTReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-simple-feasibility.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-simple-feasibility.xml");

		var vF = ValueSetResource.file("fhir/ValueSet/feasibility.xml");
		var vQT = ValueSetResource.file("fhir/ValueSet/query-type.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				"computeSimpleFeasibility/" + VERSION, Arrays.asList(aCom, sTCom, vF, cF, sTResS, sExtG, sG),
				"executeSimpleFeasibility/" + VERSION,
				Arrays.asList(aExe, sTExe, vF, cF, sR, sExtPartTtp, sExtPartMeDic, n, sG, sExtQ, vQT, cQT),
				"requestSimpleFeasibility/" + VERSION, Arrays.asList(aReq, sTReq, vF, cF, sR, sExtPartTtp,
						sExtPartMeDic, n, sG, sExtQ, vQT, cQT, sTResM, sExtG, sTErr));

		return ResourceProvider.read(VERSION,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
