package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;

import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceSecure extends AbstractServiceSecure<Task, TaskService> implements TaskService
{
	public TaskServiceSecure(TaskService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	protected Optional<String> reasonCreateNotAllowed(Task resource)
	{
		// TODO authorization rules
		// allowed status draft | requested for all users
		// task.requester must be organization of current user
		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonUpdateNotAllowed(String id, Task resource)
	{
		// TODO authorization rules
		// allowed status change from draft to requested for remote users
		// update only allowed at status draft for remote users
		// task.requester must be organization of current user or local user
		// only update of tasks with requester = current user allowed for remote users

		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonUpdateNotAllowed(Task resource, UriInfo uri)
	{
		// TODO authorization rules, see above
		return Optional.empty();
	}
}