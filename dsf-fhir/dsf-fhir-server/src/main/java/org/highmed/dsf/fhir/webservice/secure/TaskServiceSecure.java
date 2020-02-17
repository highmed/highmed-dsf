package org.highmed.dsf.fhir.webservice.secure;

import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public class TaskServiceSecure extends AbstractResourceServiceSecure<TaskDao, Task, TaskService> implements TaskService
{
	private final ActivityDefinitionDao activityDefinitionDao;

	public TaskServiceSecure(TaskService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, TaskDao taskDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ActivityDefinitionDao activityDefinitionDao)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Task.class, taskDao, exceptionHandler,
				parameterConverter);
		
		this.activityDefinitionDao = activityDefinitionDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		
		Objects.requireNonNull(activityDefinitionDao, "activityDefinitionDao");
	}

	@Override
	protected Optional<String> reasonReadAllowed(Task resource)
	{
//		if (!resource.hasRestriction() || !resource.getRestriction().hasRecipient() || !resource.hasRequester())
//			return Optional.of("Current user not Task.requester or Task.restriction.recipient");
//		else if (!isCurrentUserPartOfReferencedOrganizations("Task.restriction.recipient",
//				resource.getRestriction().getRecipient())
//				&& !isCurrentUserPartOfReferencedOrganization("Task.requester", resource.getRequester()))
//			return Optional.of("Current user not Task.requester or Task.restriction.recipient");
//		else
//			return Optional.of("");

		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonCreateAllowed(Task resource)
	{
		// FIXME authorization rules
		// allowed status draft | requested for all users
		// task.requester must be organization of current user

		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonUpdateAllowed(Task oldResource, Task newResource)
	{
		if (TaskStatus.DRAFT.equals(oldResource.getStatus()))
		{
			return Optional.of("TODO oldResource DRAFT");
		}
		else if (TaskStatus.REQUESTED.equals(oldResource.getStatus()))
		{
			return Optional.of("TODO oldResource REQUESTED");
		}
		else if (TaskStatus.COMPLETED.equals(oldResource.getStatus()))
		{
			return Optional.of("TODO oldResource REQUESTED");
		}
		else if (TaskStatus.FAILED.equals(oldResource.getStatus()))
		{
			return Optional.of("TODO oldResource REQUESTED");
		}
		// FIXME authorization rules
		// allowed status change from draft to requested for remote users
		// update only allowed at status draft for remote users
		// task.requester must be organization of current user or local user
		// only update of tasks with requester = current user allowed for remote users

		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonDeleteAllowed(Task oldResource)
	{
		// TODO authorization rules
		// allowed if oldResouce created by current user and status draft

		return Optional.empty();
	}
	
	@Override
	protected Optional<String> reasonSearchAllowed()
	{
		// TODO authorization rules

		return Optional.empty();
	}
}