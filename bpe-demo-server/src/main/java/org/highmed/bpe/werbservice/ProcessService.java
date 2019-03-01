package org.highmed.bpe.werbservice;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@Path(ProcessService.PATH)
public class ProcessService implements InitializingBean
{
	public static final String PATH = "Process";

	private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

	private final RuntimeService runtimeService;
	private final RepositoryService repositoryService;

	public ProcessService(RuntimeService runtimeService, RepositoryService repositoryService)
	{
		this.runtimeService = runtimeService;
		this.repositoryService = repositoryService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(runtimeService, "runtimeService");
		Objects.requireNonNull(repositoryService, "repositoryService");
	}

	@POST
	@Path("{processDefinitionKey}/{start : [$]start(/)?}")
	public Response startLatest(@PathParam("processDefinitionKey") String processDefinitionKey,
			@PathParam("start") String start, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		start(processDefinitionKey, null, null);

		return Response.ok("test").build();
	}

	@POST
	@Path("{processDefinitionKey}/_history/{versionTag}/{start : [$]start(/)?}")
	public Response startVersion(@PathParam("processDefinitionKey") String processDefinitionKey,
			@PathParam("versionTag") String versionTag, @PathParam("start") String start, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		start(processDefinitionKey, versionTag, null);

		return Response.ok("test").build();
	}

	private ProcessInstance start(String processDefinitionKey, String versionTag, Map<String, Object> processVariables)
	{
		ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, versionTag);

		if (processDefinition == null)
			throw new ProcessEngineException("ProcessDefinition with key " + processDefinitionKey
					+ (versionTag != null && !versionTag.isBlank() ? (" and versionTag " + versionTag) : "")
					+ " not found");

		return runtimeService.startProcessInstanceById(processDefinition.getId(), UUID.randomUUID().toString(),
				processVariables);
	}

	private ProcessDefinition getProcessDefinition(String processDefinitionKey, String versionTag)
	{
		if (versionTag != null && !versionTag.isBlank())
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.versionTag(versionTag).latestVersion().singleResult();
		else
			return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
					.latestVersion().singleResult();
	}
}
