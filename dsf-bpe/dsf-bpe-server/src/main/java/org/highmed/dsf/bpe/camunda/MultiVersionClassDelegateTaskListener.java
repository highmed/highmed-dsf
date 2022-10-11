package org.highmed.dsf.bpe.camunda;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocation;
import org.camunda.bpm.engine.impl.task.listener.ClassDelegateTaskListener;
import org.camunda.bpm.engine.impl.util.ClassDelegateUtil;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;

public class MultiVersionClassDelegateTaskListener extends ClassDelegateTaskListener
{
	private final DelegateProvider delegateProvider;

	public MultiVersionClassDelegateTaskListener(String className, List<FieldDeclaration> fieldDeclarations,
			DelegateProvider delegateProvider)
	{
		super(className, fieldDeclarations);

		this.delegateProvider = delegateProvider;
	}

	@Override
	public void notify(DelegateTask delegateTask)
	{
		TaskEntity te = (TaskEntity) delegateTask;

		ProcessKeyAndVersion processKeyAndVersion = new ProcessKeyAndVersion(te.getProcessDefinition().getKey(),
				te.getProcessDefinition().getVersionTag());

		TaskListener taskListenerInstance = getTaskListenerInstance(processKeyAndVersion);

		try
		{
			Context.getProcessEngineConfiguration().getDelegateInterceptor()
					.handleInvocation(new TaskListenerInvocation(taskListenerInstance, delegateTask));

		}
		catch (Exception e)
		{
			throw new ProcessEngineException("Exception while invoking TaskListener: " + e.getMessage(), e);
		}
	}

	protected TaskListener getTaskListenerInstance(ProcessKeyAndVersion processKeyAndVersion)
	{
		Object delegateInstance = instantiateDelegate(processKeyAndVersion, className, fieldDeclarations);

		if (delegateInstance instanceof TaskListener)
		{
			return (TaskListener) delegateInstance;
		}
		else
		{
			throw new ProcessEngineException(
					delegateInstance.getClass().getName() + " doesn't implement " + TaskListener.class);
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
