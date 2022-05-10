package org.highmed.dsf.bpe.webservice;

import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
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

	private ProcessDefinition getProcessDefinition(String processDefinitionDomain, String processDefinitionKey,
			String versionTag)
	{
		if (versionTag != null && !versionTag.isBlank())
			return repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(processDefinitionDomain + "_" + processDefinitionKey).versionTag(versionTag)
					.singleResult();
		else
			return repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(processDefinitionDomain + "_" + processDefinitionKey).latestVersion()
					.singleResult();
	}

	@GET
	@Path("/{domain}/{key}")
	public Response read(@PathParam("domain") String domain, @PathParam("key") String key, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		ProcessDefinition processDefinition = getProcessDefinition(domain, key, null);
		if (processDefinition == null)
			return Response.status(Status.NOT_FOUND).build();

		Deployment deployment = repositoryService.createDeploymentQuery()
				.deploymentId(processDefinition.getDeploymentId()).orderByDeploymentTime().desc().singleResult();

		if (deployment == null)
			return Response.status(Status.NOT_FOUND).build();

		BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());
		return Response.ok(bpmnModelInstance.getDocument().getDomSource())
				.header("Content-Disposition", "attachment;filename=" + deployment.getSource()).build();
	}

	@GET
	@Path("/{domain}/{key}/{version}")
	public Response vread(@PathParam("domain") String domain, @PathParam("key") String key,
			@PathParam("version") String version, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		ProcessDefinition processDefinition = getProcessDefinition(domain, key, version);
		if (processDefinition == null)
			return Response.status(Status.NOT_FOUND).build();

		Deployment deployment = repositoryService.createDeploymentQuery()
				.deploymentId(processDefinition.getDeploymentId()).orderByDeploymentTime().desc().singleResult();

		if (deployment == null)
			return Response.status(Status.NOT_FOUND).build();

		BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());
		return Response.ok(bpmnModelInstance.getDocument().getDomSource())
				.header("Content-Disposition", "attachment;filename=" + deployment.getSource()).build();
	}
}
