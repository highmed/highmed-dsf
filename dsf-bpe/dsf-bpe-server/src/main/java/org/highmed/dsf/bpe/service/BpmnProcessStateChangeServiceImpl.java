package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class BpmnProcessStateChangeServiceImpl implements BpmnProcessStateChangeService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(BpmnProcessStateChangeServiceImpl.class);

	private final RepositoryService repositoryService;
	private final ProcessPluginProvider processPluginProvider;

	public BpmnProcessStateChangeServiceImpl(RepositoryService repositoryService,
			ProcessPluginProvider processPluginProvider)
	{
		this.repositoryService = repositoryService;
		this.processPluginProvider = processPluginProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(repositoryService, "repositoryService");
		Objects.requireNonNull(processPluginProvider, "processPluginProvider");
	}

	@Override
	public void suspendOrActivateProcesses()
	{
		List<ProcessDefinition> deployedProcessDefinitions = repositoryService.createProcessDefinitionQuery().list();
		List<String> expectedProcessDefinitionKeysAndVersions = processPluginProvider.getDefinitions().stream()
				.map(def -> def.getProcessKeysAndVersions()).flatMap(List::stream).sorted()
				.collect(Collectors.toList());

		logger.debug("Deployed process definitions: {} (before potential state change)",
				deployedProcessDefinitions.stream()
						.map(def -> toProcessDefinitionKeyAndVersion(def.getKey(), def.getVersionTag()) + " "
								+ (def.isSuspended() ? "(suspended)" : "(active)"))
						.sorted().collect(Collectors.joining(", ", "[", "]")));
		logger.debug("Expected process definitions: {}", expectedProcessDefinitionKeysAndVersions);

		for (ProcessDefinition definition : deployedProcessDefinitions)
		{
			String definitionKeyAndVersion = toProcessDefinitionKeyAndVersion(definition.getKey(),
					definition.getVersionTag());
			if (expectedProcessDefinitionKeysAndVersions.contains(definitionKeyAndVersion) && definition.isSuspended())
			{
				logger.info("Activating process {}", definitionKeyAndVersion);
				repositoryService.activateProcessDefinitionById(definition.getId());
			}
			else if (!expectedProcessDefinitionKeysAndVersions.contains(definitionKeyAndVersion)
					&& !definition.isSuspended())
			{
				logger.info("Suspending process {}", definitionKeyAndVersion);
				repositoryService.suspendProcessDefinitionById(definition.getId());
			}
		}

		logger.info("Deployed process definitions: {}",
				repositoryService.createProcessDefinitionQuery().list().stream()
						.map(def -> toProcessDefinitionKeyAndVersion(def.getKey(), def.getVersionTag()) + " "
								+ (def.isSuspended() ? "(suspended)" : "(active)"))
						.sorted().collect(Collectors.joining(", ", "[", "]")));
	}

	private String toProcessDefinitionKeyAndVersion(String processDefinitionKey, String processDefinitionVersion)
	{
		return processDefinitionKey + "/" + processDefinitionVersion;
	}
}
