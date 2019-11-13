package org.highmed.dsf.bpe.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.highmed.dsf.bpe.Constants;
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

		return start(processDefinitionKey, null, copy(uri.getQueryParameters()));
	}

	@POST
	@Path("{processDefinitionKey}/{versionTag}/{start : [$]start(/)?}")
	public Response startVersion(@PathParam("processDefinitionKey") String processDefinitionKey,
			@PathParam("versionTag") String versionTag, @PathParam("start") String start, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		return start(processDefinitionKey, versionTag, copy(uri.getQueryParameters()));
	}

	private Response start(String processDefinitionKey, String versionTag, Map<String, List<String>> queryParameters)
	{
		ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, versionTag);

		if (processDefinition == null)
		{
			logger.warn("ProcessDefinition with key " + processDefinitionKey
					+ (versionTag != null && !versionTag.isBlank() ? (" and versionTag " + versionTag) : "")
					+ " not found");
			return Response.status(Status.NOT_FOUND).build();
		}

		runtimeService.startProcessInstanceById(processDefinition.getId(), UUID.randomUUID().toString(),
				Map.of(Constants.VARIABLE_QUERY_PARAMETERS, queryParameters));

		return Response.status(Status.CREATED).build();
	}

	private Map<String, List<String>> copy(MultivaluedMap<String, String> queryParameters)
	{
		Map<String, List<String>> map = new HashMap<>();
		queryParameters.entrySet().stream()
				.forEach(entry -> map.put(entry.getKey(), new ArrayList<String>(entry.getValue())));
		return map;
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

	// @POST
	// public Response create(BpmnModelInstance resource, @Context UriInfo uri, @Context HttpHeaders headers)
	// {
	// logger.trace("POST {}", uri.getRequestUri().toString());
	//
	// DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
	// deploymentBuilder.addModelInstance("hello_world.bpmn", resource);
	// Deployment deployment = deploymentBuilder.deploy();
	// logger.info("Hello World process deployed with ID {}", deployment.getId(), deployment);
	//
	// return Response.ok().build();
	// }

	@GET
	@Path("/{id}")
	public Response read(@PathParam("id") String id, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		ProcessDefinition processDefinition = getProcessDefinition(id, null);
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
	@Path("/{id}/{version}")
	public Response vread(@PathParam("id") String id, @PathParam("version") String version, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		ProcessDefinition processDefinition = getProcessDefinition(id, version);
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

	// @PUT
	// @Path("/{id}")
	// public Response update(@PathParam("id") String id, BpmnModelInstance resource, @Context UriInfo uri,
	// @Context HttpHeaders headers)
	// {
	// logger.trace("PUT {}", uri.getRequestUri().toString());
	//
	// return Response.ok().build();
	// }
}
