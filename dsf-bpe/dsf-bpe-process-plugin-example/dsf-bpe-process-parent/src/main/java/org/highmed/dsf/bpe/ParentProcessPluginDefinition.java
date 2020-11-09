package org.highmed.dsf.bpe;

import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.ParentConfig;

public class ParentProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("parent.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(ParentConfig.class);
	}
}
