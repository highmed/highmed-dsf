package org.highmed.dsf.bpe.service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ValidationService implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

	private ProcessEngine processEngine;
	private List<String> serviceBeans;

	public ValidationService(ProcessEngine processEngine, List<AbstractServiceDelegate> serviceBeans)
	{
		this.processEngine = processEngine;

		Objects.requireNonNull(serviceBeans, "serviceBeans");
		this.serviceBeans = serviceBeans.stream().map(b -> b.getClass().getCanonicalName())
				.collect(Collectors.toList());
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(processEngine, "processEngine");
		Objects.requireNonNull(serviceBeans, "serviceBeans");
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		RepositoryService repositoryService = processEngine.getRepositoryService();

		List<ProcessDefinition> deployedProcesses = repositoryService.createProcessDefinitionQuery().active()
				.latestVersion().list();
		List<BpmnModelInstance> models = deployedProcesses.stream()
				.map(p -> repositoryService.getBpmnModelInstance(p.getId())).collect(Collectors.toList());

		models.forEach(this::validateBeanAvailabilityForModel);
	}

	private void validateBeanAvailabilityForModel(BpmnModelInstance model)
	{
		ModelElementType processType = model.getModel().getType(Process.class);
		String processId = model.getModelElementsByType(processType).stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Process id is not set")).getAttributeValue("id");

		ModelElementType serviceType = model.getModel().getType(ServiceTask.class);
		Collection<ModelElementInstance> serviceTasks = model.getModelElementsByType(serviceType);

		serviceTasks.stream().filter(t -> t instanceof ServiceTask).map(t -> (ServiceTask) t)
				.forEach(t -> isBeanAvailableForServiceTask(t, processId));

		logger.info("Process {} passed all validations", processId);
	}

	private void isBeanAvailableForServiceTask(ServiceTask serviceTask, String processId)
	{
		String referencedClassName = serviceTask.getCamundaClass();

		if (!serviceBeans.contains(referencedClassName))
		{
			logger.error("Could not find bean for service task {} in process {}", referencedClassName, processId);
			throw new RuntimeException(
					"Could not find bean for service task " + referencedClassName + " in process " + processId);
		}
	}
}
