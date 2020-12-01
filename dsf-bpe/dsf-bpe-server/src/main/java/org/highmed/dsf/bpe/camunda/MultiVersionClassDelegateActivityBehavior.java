package org.highmed.dsf.bpe.camunda;

import java.util.List;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CustomActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskJavaDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.ClassDelegateUtil;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;

public class MultiVersionClassDelegateActivityBehavior extends ClassDelegateActivityBehavior
{
	private final DelegateProvider delegateProvider;

	public MultiVersionClassDelegateActivityBehavior(String className, List<FieldDeclaration> fieldDeclarations,
			DelegateProvider delegateProvider)
	{
		super(className, fieldDeclarations);

		this.delegateProvider = delegateProvider;
	}

	@Override
	protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution)
	{
		ExecutionEntity e = (ExecutionEntity) execution;
		ProcessKeyAndVersion processKeyAndVersion = new ProcessKeyAndVersion(e.getProcessDefinition().getKey(),
				e.getProcessDefinition().getVersionTag());

		Object delegateInstance = instantiateDelegate(processKeyAndVersion, className, fieldDeclarations);

		if (delegateInstance instanceof ActivityBehavior)
		{
			return new CustomActivityBehavior((ActivityBehavior) delegateInstance);
		}
		else if (delegateInstance instanceof JavaDelegate)
		{
			return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
		}
		else
		{
			throw LOG.missingDelegateParentClassException(delegateInstance.getClass().getName(),
					JavaDelegate.class.getName(), ActivityBehavior.class.getName());
		}
	};

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
