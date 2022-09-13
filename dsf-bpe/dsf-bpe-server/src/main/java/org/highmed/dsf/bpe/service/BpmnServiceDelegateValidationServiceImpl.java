package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
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

		deployedProcesses.stream().map(p -> repositoryService.getBpmnModelInstance(p.getId())).filter(m -> m != null)
				.flatMap(m -> m.getModelElementsByType(Process.class).stream()).filter(p -> p != null)
				.forEach(this::validateBeanAvailabilityForProcess);
	}

	private void validateBeanAvailabilityForProcess(Process process)
	{
		logger.debug("Checking bean availability for process {}/{}", process.getId(), process.getCamundaVersionTag());

		process.getChildElementsByType(ServiceTask.class).stream().filter(t -> t != null).map(t -> t.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		process.getChildElementsByType(UserTask.class).stream().filter(t -> t != null)
				.flatMap(u -> u.getChildElementsByType(ExtensionElements.class).stream()).filter(e -> e != null)
				.flatMap(e -> e.getChildElementsByType(CamundaTaskListener.class).stream()).filter(t -> t != null)
				.map(t -> t.getCamundaClass()).forEach(c -> validateBeanAvailability(process, c));

		process.getChildElementsByType(SendTask.class).stream().filter(t -> t != null).map(t -> t.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		process.getChildElementsByType(IntermediateThrowEvent.class).stream().filter(e -> e != null)
				.flatMap(e -> e.getEventDefinitions().stream()
						.filter(def -> def != null && def instanceof MessageEventDefinition))
				.map(def -> (MessageEventDefinition) def).map(def -> def.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		process.getChildElementsByType(EndEvent.class).stream().filter(e -> e != null)
				.flatMap(e -> e.getEventDefinitions().stream()
						.filter(def -> def != null && def instanceof MessageEventDefinition))
				.map(def -> (MessageEventDefinition) def).map(def -> def.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		process.getChildElementsByType(SubProcess.class).stream().filter(s -> s != null)
				.forEach(sp -> validateBeanAvailabilityForSubProcess(process, sp));
	}

	private void validateBeanAvailabilityForSubProcess(Process process, SubProcess sProcess)
	{
		sProcess.getChildElementsByType(ServiceTask.class).stream().filter(t -> t != null).map(t -> t.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		sProcess.getChildElementsByType(SendTask.class).stream().filter(t -> t != null).map(t -> t.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		sProcess.getChildElementsByType(IntermediateThrowEvent.class).stream().filter(e -> e != null)
				.flatMap(e -> e.getEventDefinitions().stream()
						.filter(def -> def != null && def instanceof MessageEventDefinition))
				.map(def -> (MessageEventDefinition) def).map(def -> def.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		sProcess.getChildElementsByType(EndEvent.class).stream().filter(e -> e != null)
				.flatMap(e -> e.getEventDefinitions().stream()
						.filter(def -> def != null && def instanceof MessageEventDefinition))
				.map(def -> (MessageEventDefinition) def).map(def -> def.getCamundaClass())
				.forEach(c -> validateBeanAvailability(process, c));

		sProcess.getChildElementsByType(SubProcess.class).stream().filter(s -> s != null)
				.forEach(sp -> validateBeanAvailabilityForSubProcess(process, sp));
	}

	private void validateBeanAvailability(Process process, String className)
	{
		if (className == null || className.isBlank())
			return;

		ProcessKeyAndVersion processKeyAndVersion = new ProcessKeyAndVersion(process.getId(),
				process.getCamundaVersionTag());

		logger.trace("Checking {} available in {}", className, processKeyAndVersion);

		Class<?> serviceClass = loadClass(processKeyAndVersion, className);
		loadBean(processKeyAndVersion, serviceClass);
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

	private void loadBean(ProcessKeyAndVersion processKeyAndVersion, Class<?> serviceClass)
	{
		try
		{
			ApplicationContext applicationContext = delegateProvider.getApplicationContext(processKeyAndVersion);
			applicationContext.getBean(serviceClass);
		}
		catch (BeansException e)
		{
			logger.error("Unable to find service delegate bean of type {} defined in process {}: {}",
					serviceClass.getName(), processKeyAndVersion, e.getMessage());
		}
	}
}
