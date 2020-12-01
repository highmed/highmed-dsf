package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

public class BpmnServiceDelegateValidationServiceImpl implements BpmnServiceDelegateValidationService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(BpmnServiceDelegateValidationServiceImpl.class);

	private final ProcessEngine processEngine;
	private final DelegateProvider delegateProvider;

	public BpmnServiceDelegateValidationServiceImpl(ProcessEngine processEngine, DelegateProvider delegateProvider)
	{
		this.processEngine = processEngine;
		this.delegateProvider = delegateProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(processEngine, "processEngine");
		Objects.requireNonNull(delegateProvider, "delegateProvider");
	}

	@Override
	public void validateModels()
	{
		logger.debug("Validating bpmn models, checking service delegate availability");

		RepositoryService repositoryService = processEngine.getRepositoryService();

		List<ProcessDefinition> deployedProcesses = repositoryService.createProcessDefinitionQuery().active()
				.latestVersion().list();
		List<BpmnModelInstance> models = deployedProcesses.stream()
				.map(p -> repositoryService.getBpmnModelInstance(p.getId())).collect(Collectors.toList());

		models.forEach(this::validateBeanAvailabilityForModel);
	}

	private void validateBeanAvailabilityForModel(BpmnModelInstance model)
	{
		model.getModelElementsByType(Process.class).stream().forEach(this::validateBeanAvailabilityForProcess);
	}

	private void validateBeanAvailabilityForProcess(Process process)
	{
		process.getChildElementsByType(ServiceTask.class).stream()
				.forEach(task -> validateBeanAvailabilityForTask(
						new ProcessKeyAndVersion(process.getId(), process.getCamundaVersionTag()),
						task.getCamundaClass()));
	}

	private void validateBeanAvailabilityForTask(ProcessKeyAndVersion processKeyAndVersion, String className)
	{
		Class<?> serviceClass = loadClass(processKeyAndVersion, className);
		loadBean(processKeyAndVersion, serviceClass);
	}

	private void loadBean(ProcessKeyAndVersion processKeyAndVersion, Class<?> serviceClass)
	{
		try
		{
			ApplicationContext applicationContext = delegateProvider.getApplicationContext(processKeyAndVersion);
			applicationContext.getBean(serviceClass);
		}
		catch (BeansException e)
		{
			logger.warn("Error while getting service delegate bean of type {} defined in process {} not found",
					serviceClass.getName(), processKeyAndVersion);
			throw new RuntimeException(e);
		}
	}

	private Class<?> loadClass(ProcessKeyAndVersion processKeyAndVersion, String className)
	{
		try
		{
			ClassLoader classLoader = delegateProvider.getClassLoader(processKeyAndVersion);
			return classLoader.loadClass(className);
		}
		catch (ClassNotFoundException e)
		{
			logger.warn("Service delegate class {} defined in process {} not found", className, processKeyAndVersion);
			throw new RuntimeException(e);
		}
	}
}
