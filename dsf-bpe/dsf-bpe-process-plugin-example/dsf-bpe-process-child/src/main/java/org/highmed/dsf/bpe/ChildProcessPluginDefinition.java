package org.highmed.dsf.bpe;

import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.ChildConfig;

public class ChildProcessPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public String getVersion()
	{
		return "0.4.0";
	}

	@Override
	public String getName()
	{
		return "dsf-bpe-process-child-0.4.0";
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("child.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(ChildConfig.class);
	}
}
