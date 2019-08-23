package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractProcessEnginePlugin implements ProcessEnginePlugin
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractProcessEnginePlugin.class);

	private DefaultBpmnParseListener defaultBpmnParseListener;

	public AbstractProcessEnginePlugin(DefaultBpmnParseListener defaultBpmnParseListener) {
		this.defaultBpmnParseListener = defaultBpmnParseListener;
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
		processEngineConfiguration.setCustomPreBPMNParseListeners(List.of(defaultBpmnParseListener));
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	protected void delpoy(ProcessEngine processEngine, String modelFilename, BpmnModelInstance model)
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
		return model;
	}
}
