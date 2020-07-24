package org.highmed.dsf.fhir.variables;

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

public class SerializerPlugin implements ProcessEnginePlugin
{
	@SuppressWarnings("rawtypes")
	private List<TypedValueSerializer> serializer;

	public SerializerPlugin(FhirResourceSerializer fhirResourceSerializer,
			FhirResourcesListSerializer fhirResourcesListSerializer,
			MultiInstanceTargetSerializer multiInstanceTargetSerializer,
			MultiInstanceTargetsSerializer multiInstanceTargetsSerializer, OutputSerializer outputSerializer,
			OutputsSerializer outputsSerializer)
	{
		serializer = Arrays.asList(fhirResourceSerializer, fhirResourcesListSerializer, multiInstanceTargetSerializer,
				multiInstanceTargetsSerializer, outputSerializer, outputsSerializer);
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
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
