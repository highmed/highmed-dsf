package org.highmed.dsf.bpe.delegate;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

public class DelegateProviderImpl implements DelegateProvider, InitializingBean
{
	private final Map<String, ClassLoader> classLoaderByProcessDefinitionKeyAndVersion;
	private final ClassLoader defaultClassLoader;
	private final Map<String, ApplicationContext> applicationContextByProcessDefinitionKeyAndVersion;
	private final ApplicationContext defaultApplicationContext;

	public DelegateProviderImpl(Map<String, ClassLoader> classLoaderByProcessDefinitionKeyAndVersion,
			ClassLoader defaultClassLoader,
			Map<String, ApplicationContext> applicationContextByProcessDefinitionKeyAndVersion,
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
	public ClassLoader getClassLoader(String processDefinitionKey, String processDefinitionVersion)
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(processDefinitionVersion, "processDefinitionVersion");

		return classLoaderByProcessDefinitionKeyAndVersion.getOrDefault(
				toProcessDefinitionKeyAndVersion(processDefinitionKey, processDefinitionVersion), defaultClassLoader);
	}

	@Override
	public ApplicationContext getApplicationContext(String processDefinitionKey, String processDefinitionVersion)
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(processDefinitionVersion, "processDefinitionVersion");

		return applicationContextByProcessDefinitionKeyAndVersion.getOrDefault(
				toProcessDefinitionKeyAndVersion(processDefinitionKey, processDefinitionVersion),
				defaultApplicationContext);
	}

	private String toProcessDefinitionKeyAndVersion(String processDefinitionKey, String processDefinitionVersion)
	{
		return processDefinitionKey + "/" + processDefinitionVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Stream<TypedValueSerializer> getAdditionalTypedValueSerializers()
	{
		return applicationContextByProcessDefinitionKeyAndVersion.values().stream()
				.map(ctx -> ctx.getBeansOfType(TypedValueSerializer.class)).flatMap(m -> m.values().stream());
	}
}
