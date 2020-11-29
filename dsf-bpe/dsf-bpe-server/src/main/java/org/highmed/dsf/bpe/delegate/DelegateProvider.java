package org.highmed.dsf.bpe.delegate;

import java.util.List;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.springframework.context.ApplicationContext;

public interface DelegateProvider
{
	/**
	 * @param processKeyAndVersion
	 *            not <code>null</code>
	 * @return returns the default class loader if no special class loader is registered for the given
	 *         <b>processDefinitionKey</b> and <b>processDefinitionVersion</b>
	 */
	ClassLoader getClassLoader(ProcessKeyAndVersion processKeyAndVersion);

	/**
	 * @param processKeyAndVersion
	 *            not <code>null</code>
	 * @return returns the default application context if no special application context is registered for the given
	 *         <b>processDefinitionKey</b> and <b>processDefinitionVersion</b>
	 */
	ApplicationContext getApplicationContext(ProcessKeyAndVersion processKeyAndVersion);

	/**
	 * @return additional {@link TypedValueSerializer}s from the plugin to deploy into the process engine
	 */
	@SuppressWarnings("rawtypes")
	List<TypedValueSerializer> getTypedValueSerializers();
}
