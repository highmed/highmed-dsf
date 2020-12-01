package org.highmed.dsf.bpe.delegate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

public class DelegateProviderImpl implements DelegateProvider, InitializingBean
{
	private final Map<ProcessKeyAndVersion, ClassLoader> classLoaderByProcessDefinitionKeyAndVersion;
	private final ClassLoader defaultClassLoader;
	private final Map<ProcessKeyAndVersion, ApplicationContext> applicationContextByProcessDefinitionKeyAndVersion;
	private final ApplicationContext defaultApplicationContext;

	public DelegateProviderImpl(Map<ProcessKeyAndVersion, ClassLoader> classLoaderByProcessDefinitionKeyAndVersion,
			ClassLoader defaultClassLoader,
			Map<ProcessKeyAndVersion, ApplicationContext> applicationContextByProcessDefinitionKeyAndVersion,
			ApplicationContext defaultApplicationContext)
	{
		this.classLoaderByProcessDefinitionKeyAndVersion = classLoaderByProcessDefinitionKeyAndVersion;
		this.defaultClassLoader = defaultClassLoader;
		this.applicationContextByProcessDefinitionKeyAndVersion = applicationContextByProcessDefinitionKeyAndVersion;
		this.defaultApplicationContext = defaultApplicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(classLoaderByProcessDefinitionKeyAndVersion,
				"classLoaderByProcessDefinitionKeyAndVersion");
		Objects.requireNonNull(defaultClassLoader, "defaultClassLoader");
		Objects.requireNonNull(applicationContextByProcessDefinitionKeyAndVersion,
				"applicationContextByProcessDefinitionKeyAndVersion");
		Objects.requireNonNull(defaultApplicationContext, "defaultApplicationContext");
	}

	@Override
	public ClassLoader getClassLoader(ProcessKeyAndVersion processKeyAndVersion)
	{
		Objects.requireNonNull(processKeyAndVersion, "processKeyAndVersion");

		return classLoaderByProcessDefinitionKeyAndVersion.getOrDefault(processKeyAndVersion, defaultClassLoader);
	}

	@Override
	public ApplicationContext getApplicationContext(ProcessKeyAndVersion processKeyAndVersion)
	{
		Objects.requireNonNull(processKeyAndVersion, "processKeyAndVersion");

		return applicationContextByProcessDefinitionKeyAndVersion.getOrDefault(processKeyAndVersion,
				defaultApplicationContext);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<TypedValueSerializer> getTypedValueSerializers()
	{
		return applicationContextByProcessDefinitionKeyAndVersion.values().stream()
				.map(ctx -> ctx.getBeansOfType(TypedValueSerializer.class)).distinct().flatMap(m -> m.values().stream())
				.collect(Collectors.toList());
	}
}
