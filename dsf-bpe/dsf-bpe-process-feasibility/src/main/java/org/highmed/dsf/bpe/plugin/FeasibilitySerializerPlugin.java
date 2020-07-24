package org.highmed.dsf.bpe.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.highmed.dsf.bpe.variables.BloomFilterConfigSerializer;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsSerializer;

public class FeasibilitySerializerPlugin implements ProcessEnginePlugin
{
	@SuppressWarnings("rawtypes")
	private final List<TypedValueSerializer> serializer;

	public FeasibilitySerializerPlugin(FeasibilityQueryResultSerializer feasibilityQueryResultSerializer,
			FeasibilityQueryResultsSerializer feasibilityQueryResultsSerializer,
			FinalFeasibilityQueryResultSerializer finalFeasibilityQueryResultSerializer,
			FinalFeasibilityQueryResultsSerializer finalFeasibilityQueryResultsSerializer,
			BloomFilterConfigSerializer bloomFilterConfigSerializer)
	{
		serializer = Arrays.asList(feasibilityQueryResultSerializer, feasibilityQueryResultsSerializer,
				finalFeasibilityQueryResultSerializer, finalFeasibilityQueryResultsSerializer,
				bloomFilterConfigSerializer);
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<TypedValueSerializer> initialSerializers = Optional.ofNullable(processEngineConfiguration
				.getCustomPreVariableSerializers()).orElse(Collections.emptyList());

		List<TypedValueSerializer> allSerializer = Stream.concat(initialSerializers.stream(), serializer.stream())
				.collect(Collectors.toList());

		processEngineConfiguration.setCustomPreVariableSerializers(allSerializer);
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
