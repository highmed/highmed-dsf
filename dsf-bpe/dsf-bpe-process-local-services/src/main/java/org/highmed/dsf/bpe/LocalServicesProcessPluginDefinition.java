package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.LocalServicesConfig;

public class LocalServicesProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("localServicesIntegration.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(LocalServicesConfig.class);
	}

	@Override
	public List<String> getDependencyJarNames()
	{
		return Arrays.asList("dsf-bpe-process-feasibility-0.4.0");
	}
}
