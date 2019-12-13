package org.highmed.dsf.bpe.plugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public abstract class AbstractProcessEnginePlugin implements ProcessEnginePlugin
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractProcessEnginePlugin.class);

	private final ApplicationContext context;

	public AbstractProcessEnginePlugin(ApplicationContext context)
	{
		this.context = context;
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	protected void deploy(ProcessEngine processEngine, String modelFilename, BpmnModelInstance model)
	{
		RepositoryService repositoryService = processEngine.getRepositoryService();

		Deployment deployment = repositoryService.createDeployment().name(modelFilename).source(modelFilename)
				.addModelInstance(modelFilename, model).enableDuplicateFiltering(true).deploy();

		logger.info("Process {} deployed with id {}", modelFilename, deployment.getId());
	}

	protected BpmnModelInstance readAndValidateModel(String modelFilename)
	{
		BpmnModelInstance model = Bpmn
				.readModelFromStream(AbstractProcessEnginePlugin.class.getResourceAsStream(modelFilename));

		Bpmn.validateModel(model);
		validateServiceTaskBeanAvailability(model);

		return model;
	}

	private void validateServiceTaskBeanAvailability(BpmnModelInstance model)
	{
		ModelElementType serviceType = model.getModel().getType(ServiceTask.class);
		Collection<ModelElementInstance> serviceTasks = model.getModelElementsByType(serviceType);

		List<String> beans = context.getBeansOfType(JavaDelegate.class).values().stream()
				.map(b -> b.getClass().getCanonicalName()).collect(Collectors.toList());

		ModelElementType processType = model.getModel().getType(Process.class);
		String processId = model.getModelElementsByType(processType).stream().findFirst()
				.orElseThrow(() -> new BpmnModelException("Process id is not set")).getAttributeValue("id");

		serviceTasks.forEach(t -> {
			ServiceTask serviceTask = (ServiceTask) t;
			if (!beans.contains(serviceTask.getCamundaClass()))
				throw new NoSuchBeanDefinitionException(
						serviceTask.getCamundaClass() + " referenced in process with id " + processId);
		});
	}
}
