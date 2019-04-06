package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceSecure extends AbstractServiceSecure<Task, TaskService> implements TaskService
{
	public TaskServiceSecure(TaskService delegate)
	{
		super(delegate);
	}

	@Override
	public Response create(Task resource, UriInfo uri, HttpHeaders headers)
	{
		// allowed status draft | requested for all users
		// task.requester must be organization of current user

		// TODO Auto-generated method stub
		return super.create(resource, uri, headers);
	}

	@Override
	public Response update(String id, Task resource, UriInfo uri, HttpHeaders headers)
	{
		// allowed status change from draft to requested for remote users
		// update only allowed at status draft for remote users
		// task.requester must be organization of current user or local user
		// only update of tasks with requester = current user allowed for remote users

		// TODO Auto-generated method stub
		return super.update(id, resource, uri, headers);
	}

	@Override
	public Response update(Task resource, UriInfo uri, HttpHeaders headers)
	{
		// see update above

		// TODO Auto-generated method stub
		return super.update(resource, uri, headers);
	}
}
