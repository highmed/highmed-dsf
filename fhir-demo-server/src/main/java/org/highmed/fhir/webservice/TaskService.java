package org.highmed.fhir.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.TaskDao;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.rest.api.Constants;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<TaskDao, Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, TaskDao taskDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, taskDao);
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("requester") String requester, @Context UriInfo uri)
	{
		return Response.status(Status.NOT_FOUND).build();
	}
}
