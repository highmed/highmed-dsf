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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class BpmnServiceDelegateValidationService implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(BpmnServiceDelegateValidationService.class);

	private final ProcessEngine processEngine;
	private final DelegateProvider delegateProvider;

	public BpmnServiceDelegateValidationService(ProcessEngine processEngine, DelegateProvider delegateProvider)
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

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
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
				.forEach(task -> validateBeanAvailabilityForTask(process.getId(), process.getCamundaVersionTag(),
						task.getCamundaClass()));
	}

	private void validateBeanAvailabilityForTask(String processDefinitionKey, String processDefinitionVersion,
			String className)
	{
		Class<?> serviceClass = loadClass(processDefinitionKey, processDefinitionVersion, className);
		loadBean(processDefinitionKey, processDefinitionVersion, serviceClass);
	}

	private void loadBean(String processDefinitionKey, String processDefinitionVersion, Class<?> serviceClass)
	{
		try
		{
			ApplicationContext applicationContext = delegateProvider.getApplicationContext(processDefinitionKey,
					processDefinitionVersion);
			applicationContext.getBean(serviceClass);
		}
		catch (BeansException e)
		{
			logger.warn("Error while getting service delegate bean of type {} defined in process {}/{} not found",
					serviceClass.getName(), processDefinitionKey, processDefinitionVersion);
			throw new RuntimeException(e);
		}
	}

	private Class<?> loadClass(String processDefinitionKey, String processDefinitionVersion, String className)
	{
		try
		{
			ClassLoader classLoader = delegateProvider.getClassLoader(processDefinitionKey, processDefinitionVersion);
			return classLoader.loadClass(className);
		}
		catch (ClassNotFoundException e)
		{
			logger.warn("Service delegate class {} defined in process {}/{} not found", className, processDefinitionKey,
					processDefinitionVersion);
			throw new RuntimeException(e);
		}
	}
}
