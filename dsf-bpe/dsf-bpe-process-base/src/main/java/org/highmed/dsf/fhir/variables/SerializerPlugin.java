package org.highmed.dsf.fhir.variables;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;

public class SerializerPlugin implements ProcessEnginePlugin
{
	@SuppressWarnings("rawtypes")
	private final List<TypedValueSerializer> serializer;

	public SerializerPlugin(FhirResourceSerializer fhirResourceSerializer,
			FhirResourcesListSerializer fhirResourcesListSerializer,
			MultiInstanceTargetSerializer multiInstanceTargetSerializer,
			MultiInstanceTargetsSerializer multiInstanceTargetsSerializer,
			FeasibilityQueryResultSerializer feasibilityQueryResultSerializer,
			FeasibilityQueryResultsSerializer feasibilityQueryResultsSerializer,
			FinalFeasibilityQueryResultSerializer finalFeasibilityQueryResultSerializer,
			FinalFeasibilityQueryResultsSerializer finalFeasibilityQueryResultsSerializer,
			BloomFilterConfigSerializer bloomFilterConfigSerializer, OutputSerializer outputSerializer,
			OutputsSerializer outputsSerializer)
	{
		serializer = Arrays.asList(fhirResourceSerializer, fhirResourcesListSerializer, multiInstanceTargetSerializer,
				multiInstanceTargetsSerializer, feasibilityQueryResultSerializer, feasibilityQueryResultsSerializer,
				finalFeasibilityQueryResultSerializer, finalFeasibilityQueryResultsSerializer,
				bloomFilterConfigSerializer, outputSerializer, outputsSerializer);
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
		processEngineConfiguration.setCustomPreVariableSerializers(serializer);
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
	}
}
