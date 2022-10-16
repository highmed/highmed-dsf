package org.highmed.dsf.bpe.camunda;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.delegate.ExecutionListenerInvocation;
import org.camunda.bpm.engine.impl.bpmn.listener.ClassDelegateExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClassDelegateUtil;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;

public class MultiVersionClassDelegateExecutionListener extends ClassDelegateExecutionListener
{
	private final DelegateProvider delegateProvider;

	public MultiVersionClassDelegateExecutionListener(String className, List<FieldDeclaration> fieldDeclarations,
			DelegateProvider delegateProvider)
	{
		super(className, fieldDeclarations);

		this.delegateProvider = delegateProvider;
	}

	@Override
	public void notify(DelegateExecution execution)
	{
		ExecutionEntity e = (ExecutionEntity) execution;

		ProcessKeyAndVersion processKeyAndVersion = new ProcessKeyAndVersion(e.getProcessDefinition().getKey(),
				e.getProcessDefinition().getVersionTag());

		ExecutionListener executionListenerInstance = getExecutionListenerInstance(processKeyAndVersion);

		try
		{
			Context.getProcessEngineConfiguration().getDelegateInterceptor()
					.handleInvocation(new ExecutionListenerInvocation(executionListenerInstance, execution));

		}
		catch (Exception exception)
		{
			throw new ProcessEngineException("Exception while invoking ExecutionListener: " + exception.getMessage(),
					exception);
		}
	}

	protected ExecutionListener getExecutionListenerInstance(ProcessKeyAndVersion processKeyAndVersion)
	{
		Object delegateInstance = instantiateDelegate(processKeyAndVersion, className, fieldDeclarations);

		if (delegateInstance instanceof ExecutionListener)
		{
			return (ExecutionListener) delegateInstance;
		}
		else
		{
			throw new ProcessEngineException(
					delegateInstance.getClass().getName() + " doesn't implement " + ExecutionListener.class);
		}
	}

	private Object instantiateDelegate(ProcessKeyAndVersion processKeyAndVersion, String className,
			List<FieldDeclaration> fieldDeclarations)
	{
		try
		{
			Class<?> clazz = delegateProvider.getClassLoader(processKeyAndVersion).loadClass(className);
			Object bean = delegateProvider.getApplicationContext(processKeyAndVersion).getBean(clazz);

			ClassDelegateUtil.applyFieldDeclaration(fieldDeclarations, bean);
			return bean;
		}
		catch (Exception e)
		{
			throw ProcessEngineLogger.UTIL_LOGGER.exceptionWhileInstantiatingClass(className, e);
		}
	}
}
