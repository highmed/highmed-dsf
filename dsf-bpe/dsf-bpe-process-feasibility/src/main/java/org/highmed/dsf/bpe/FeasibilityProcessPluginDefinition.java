package org.highmed.dsf.bpe;

import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.FeasibilityConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;

public class FeasibilityProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("requestSimpleFeasibility.bpmn","computeSimpleFeasibility.bpmn", "executeSimpleFeasibility.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(FeasibilityConfig.class, FeasibilitySerializerConfig.class);
	}
}
