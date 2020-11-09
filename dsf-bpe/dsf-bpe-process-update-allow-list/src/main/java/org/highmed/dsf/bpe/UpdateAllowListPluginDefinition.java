package org.highmed.dsf.bpe;

import java.util.stream.Stream;

import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.spring.config.UpdateAllowListConfig;

public class UpdateAllowListPluginDefinition implements ProcessPluginDefinition
{
	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("updateAllowList.bpmn", "downloadAllowList.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(UpdateAllowListConfig.class);
	}
}
