package org.highmed.dsf.bpe.service;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.highmed.dsf.bpe.dao.ProcessStateDao;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.highmed.dsf.bpe.process.BpmnFileAndModel;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessState;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class BpmnProcessStateChangeServiceImpl implements BpmnProcessStateChangeService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(BpmnProcessStateChangeServiceImpl.class);

	private final RepositoryService repositoryService;
	private final ProcessStateDao processStateDao;
	private final ProcessPluginProvider processPluginProvider;
	private final List<ProcessKeyAndVersion> excluded = new ArrayList<>();
	private final List<ProcessKeyAndVersion> retired = new ArrayList<>();
	private final List<ProcessKeyAndVersion> draft = new ArrayList<>();

	public BpmnProcessStateChangeServiceImpl(RepositoryService repositoryService, ProcessStateDao processStateDao,
			ProcessPluginProvider processPluginProvider, List<ProcessKeyAndVersion> excluded,
			List<ProcessKeyAndVersion> retired)
	{
		this.repositoryService = repositoryService;
		this.processStateDao = processStateDao;
		this.processPluginProvider = processPluginProvider;

		if (excluded != null)
			this.excluded.addAll(excluded);
		if (retired != null)
			this.retired.addAll(retired);

		this.draft.addAll(processPluginProvider.getDraftProcessKeyAndVersions());
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(repositoryService, "repositoryService");
		Objects.requireNonNull(processStateDao, "processStateDao");
		Objects.requireNonNull(processPluginProvider, "processPluginProvider");
	}

	private Map<ProcessKeyAndVersion, ProcessState> getStates()
	{
		try
		{
			return processStateDao.getStates();
		}
		catch (SQLException e)
		{
			logger.warn("Error while retrieving process states from db", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ProcessStateChangeOutcome> deploySuspendOrActivateProcesses(Stream<BpmnFileAndModel> models)
	{
		Objects.requireNonNull(models, "models");

		Map<ProcessKeyAndVersion, ProcessState> oldProcessStates = getStates();
		Map<ProcessKeyAndVersion, ProcessState> newProcessStates = new HashMap<>();

		logger.debug("Deploying process models ...");
		models.forEach(this::deploy);

		List<ProcessKeyAndVersion> loadedProcesses = processPluginProvider.getProcessKeyAndVersions();

		List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
		for (ProcessDefinition definition : definitions)
		{
			ProcessKeyAndVersion process = ProcessKeyAndVersion.fromDefinition(definition);

			ProcessState oldState = oldProcessStates.getOrDefault(process, ProcessState.NEW);

			ProcessState newState = loadedProcesses.contains(process) ? ProcessState.ACTIVE : ProcessState.EXCLUDED;
			if (excluded.contains(process))
				newState = ProcessState.EXCLUDED;
			else if (retired.contains(process))
				newState = ProcessState.RETIRED;
			else if (draft.contains(process))
				newState = ProcessState.DRAFT;

			newProcessStates.put(process, newState);

			logger.debug("{}: {} -> {}", process.toString(), oldState, newState);

			// NEW -> ACTIVE : - (new process active by default)
			// NEW -> DRAFT : - (new process active by default)
			// NEW -> RETIRED : suspend
			// NEW -> EXCLUDED : suspend
			// ACTIVE -> ACTIVE : -
			// ACTIVE -> DRAFT : -
			// ACTIVE -> RETIRED : suspend
			// ACTIVE -> EXCLUDED : suspend
			// DRAFT -> ACTIVE : -
			// DRAFT -> DRAFT : -
			// DRAFT -> RETIRED : suspend
			// DRAFT -> EXCLUDED : suspend
			// RETIRED -> ACTIVE : activate
			// RETIRED -> DRAFT : activate
			// RETIRED -> RETIRED : -
			// RETIRED -> EXCLUDED : -
			// EXCLUDED -> ACTIVE : activate
			// EXCLUDED -> DRAFT : activate
			// EXCLUDED -> RETIRED : -
			// EXCLUDED -> EXCLUDED : -

			if ((ProcessState.RETIRED.equals(oldState) && ProcessState.ACTIVE.equals(newState))
					|| (ProcessState.RETIRED.equals(oldState) && ProcessState.DRAFT.equals(newState))
					|| (ProcessState.EXCLUDED.equals(oldState) && ProcessState.ACTIVE.equals(newState))
					|| (ProcessState.EXCLUDED.equals(oldState) && ProcessState.DRAFT.equals(newState)))
			{
				logger.debug("Activating process {}", process.toString());
				repositoryService.activateProcessDefinitionById(definition.getId());
			}
			else if ((ProcessState.NEW.equals(oldState) && ProcessState.RETIRED.equals(newState))
					|| (ProcessState.NEW.equals(oldState) && ProcessState.EXCLUDED.equals(newState))
					|| (ProcessState.ACTIVE.equals(oldState) && ProcessState.RETIRED.equals(newState))
					|| (ProcessState.ACTIVE.equals(oldState) && ProcessState.EXCLUDED.equals(newState))
					|| (ProcessState.DRAFT.equals(oldState) && ProcessState.RETIRED.equals(newState))
					|| (ProcessState.DRAFT.equals(oldState) && ProcessState.EXCLUDED.equals(newState)))
			{
				logger.debug("Suspending process {}", process.toString());
				repositoryService.suspendProcessDefinitionById(definition.getId());
			}
		}

		updateStates(newProcessStates);

		logProcessDeploymentStatus();

		return newProcessStates.entrySet().stream()
				.map(e -> new ProcessStateChangeOutcome(e.getKey(),
						oldProcessStates.getOrDefault(e.getKey(), ProcessState.NEW), e.getValue()))
				.collect(Collectors.toList());
	}

	private void updateStates(Map<ProcessKeyAndVersion, ProcessState> states)
	{
		try
		{
			processStateDao.updateStates(states);
		}
		catch (SQLException e)
		{
			logger.warn("Error while updating process states in db", e);
			throw new RuntimeException(e);
		}
	}

	private void logProcessDeploymentStatus()
	{
		Map<String, Deployment> deploymentsById = repositoryService.createDeploymentQuery().orderByDeploymentName()
				.asc().orderByDeploymentTime().desc().list().stream()
				.collect(Collectors.toMap(Deployment::getId, Function.identity()));

		List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
				.orderByProcessDefinitionKey().asc().orderByVersionTag().asc().orderByProcessDefinitionVersion().asc()
				.list();

		// standard for-each loop to produce cleaner log messages
		for (ProcessDefinition def : definitions)
		{
			Deployment dep = deploymentsById.get(def.getDeploymentId());

			if (def.isSuspended())
				logger.debug("Suspended process {}/{} (internal version {}) from {} deployed {}", def.getKey(),
						def.getVersionTag(), def.getVersion(), dep.getSource(), dep.getDeploymentTime());
			else
				logger.info("Active process {}/{} (internal version {}) from {} deployed {}", def.getKey(),
						def.getVersionTag(), def.getVersion(), dep.getSource(), dep.getDeploymentTime());
		}
	}

	private void deploy(BpmnFileAndModel fileAndModel)
	{
		ProcessKeyAndVersion processKeyAndVersion = fileAndModel.getProcessKeyAndVersion();

		DeploymentBuilder builder = repositoryService.createDeployment().name(processKeyAndVersion.toString())
				.source(fileAndModel.getFile()).addModelInstance(fileAndModel.getFile(), fileAndModel.getModel())
				.enableDuplicateFiltering(true);

		Deployment deployment = builder.deploy();

		logger.debug("Process {} from {}://{} deployed with id {}", processKeyAndVersion.toString(),
				fileAndModel.getJars().stream().map(Path::toString).collect(Collectors.joining("; ")),
				fileAndModel.getFile(), deployment.getId());

		if (draft.contains(processKeyAndVersion))
		{
			List<ProcessDefinition> activeDraftDefinitions = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(processKeyAndVersion.getKey()).versionTag(processKeyAndVersion.getVersion())
					.orderByDeploymentTime().desc().active().list();

			activeDraftDefinitions.stream().skip(1).forEach(def ->
			{
				logger.debug("Suspending existing draft process definition {} from deployment with id {}",
						processKeyAndVersion.toString(), def.getDeploymentId());
				repositoryService.suspendProcessDefinitionById(def.getId());
			});
		}
	}
}
