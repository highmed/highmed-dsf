package org.highmed.fhir.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.rest.api.Constants;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<TaskDao, Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, int defaultPageCount, TaskDao taskDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, taskDao);
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("requester") String requester, @QueryParam("status") String status,
			@QueryParam("page") Integer page, @QueryParam("_count") Integer count, @QueryParam("_format") String format,
			@Context UriInfo uri)
	{
		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? getDefaultPageCount() : count;

		PartialResult<Task> tasks = handleSql(() -> getDao().search(requester, status, effectivePage, effectiveCount));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();

		if (requester != null && !requester.isBlank())
			bundleUri = bundleUri.replaceQueryParam("requester", requester);
		if (status != null && !status.isBlank() && statusValid(status))
			bundleUri = bundleUri.replaceQueryParam("status", status);
		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);

		return response(Status.OK, createSearchSet(tasks, bundleUri), toSpecialMimeType(format)).build();
	}

	private boolean statusValid(String status)
	{
		// FIXME control flow by exception
		try
		{
			TaskStatus.fromCode(status);
			return true;
		}
		catch (FHIRException e)
		{
			return false;
		}
	}
}
