package org.highmed.dsf.bpe.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.ProcessPluginDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ProcessPluginDefinitionAndClassLoader
{
	private final List<Path> jars = new ArrayList<>();
	private final ProcessPluginDefinition definition;
	private final ClassLoader classLoader;

	private ProcessPlugin processPlugin;

	public ProcessPluginDefinitionAndClassLoader(List<Path> jars, ProcessPluginDefinition definition,
			ClassLoader classLoader)
	{
		if (jars != null)
			this.jars.addAll(jars);

		this.definition = definition;
		this.classLoader = classLoader;
	}

	public List<Path> getJars()
	{
		return Collections.unmodifiableList(jars);
	}

	public ProcessPluginDefinition getDefinition()
	{
		return definition;
	}

	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	public ApplicationContext createPluginApplicationContext(ApplicationContext mainContext)
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setParent(mainContext);
		context.setClassLoader(getClassLoader());
		context.register(getDefinition().getSpringConfigClasses().toArray(Class<?>[]::new));
		context.refresh();
		return context;
	}

	public ProcessPlugin getProcessPlugin()
	{
		if (processPlugin == null)
			processPlugin = ProcessPlugin.loadAndValidateModels(jars, getDefinition().getBpmnFiles(), getClassLoader());

		return processPlugin;
	}

	public List<String> getProcessKeysAndVersions()
	{
		return getProcessPlugin().getProcessKeysAndVersions();
	}
}
