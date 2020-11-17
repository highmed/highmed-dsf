package org.highmed.dsf.bpe.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessPlugin implements ProcessEnginePlugin
{
	private static final Logger logger = LoggerFactory.getLogger(ProcessPlugin.class);

	private static final class BpmnFileAndModel
	{
		final String file;
		final BpmnModelInstance model;

		BpmnFileAndModel(String file, BpmnModelInstance model)
		{
			this.file = file;
			this.model = model;
		}
	}

	private final List<Path> jars = new ArrayList<>();
	private final List<BpmnFileAndModel> models = new ArrayList<>();

	public static ProcessPlugin loadAndValidateModels(List<Path> jars, Stream<String> bpmnFiles,
			ClassLoader classLoader)
	{
		List<BpmnFileAndModel> models = bpmnFiles.map(file -> loadAndValidateModel(file, classLoader))
				.collect(Collectors.toList());
		return new ProcessPlugin(jars, models);
	}

	private static BpmnFileAndModel loadAndValidateModel(String bpmnFile, ClassLoader classLoader)
	{
		BpmnModelInstance model = Bpmn.readModelFromStream(classLoader.getResourceAsStream(bpmnFile));
		Bpmn.validateModel(model);
		return new BpmnFileAndModel(bpmnFile, model);
	}

	private ProcessPlugin(List<Path> jars, List<BpmnFileAndModel> models)
	{
		if (jars != null)
			this.jars.addAll(jars);
		if (models != null)
			this.models.addAll(models);
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		models.forEach(fileAndModel -> deploy(processEngine, fileAndModel));
	}

	public List<ProcessKeyAndVersion> getProcessKeysAndVersions()
	{
		return models.stream().flatMap(fileAndModel ->
		{
			Collection<Process> processes = fileAndModel.model.getModelElementsByType(Process.class);
			return processes.stream().map(p -> new ProcessKeyAndVersion(p.getId(), p.getCamundaVersionTag()));
		}).collect(Collectors.toList());
	}

	private void deploy(ProcessEngine processEngine, BpmnFileAndModel fileAndModel)
	{
		RepositoryService repositoryService = processEngine.getRepositoryService();

		Deployment deployment = repositoryService.createDeployment().name(fileAndModel.file).source(fileAndModel.file)
				.addModelInstance(fileAndModel.file, fileAndModel.model).enableDuplicateFiltering(true).deploy();

		if (logger.isInfoEnabled())
		{
			Collection<Process> processes = fileAndModel.model.getModelElementsByType(Process.class);
			String processDefinitionKeysAndVersions = processes.stream()
					.map(p -> p.getId() + "/" + p.getCamundaVersionTag()).collect(Collectors.joining(", "));

			logger.info("Process{} {} from {}://{} deployed with id {}", processes.size() > 1 ? "es" : "",
					processDefinitionKeysAndVersions,
					jars.stream().map(Path::toString).collect(Collectors.joining("; ")), fileAndModel.file,
					deployment.getId());
		}
	}
}
