package org.highmed.dsf.bpe;

import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.PingConfig;

public class PingProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("ping.bpmn", "pong.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(PingConfig.class);
	}
}
