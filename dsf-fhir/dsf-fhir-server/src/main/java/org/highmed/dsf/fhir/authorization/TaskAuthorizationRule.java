package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.hl7.fhir.r4.model.Task;

public class TaskAuthorizationRule extends AbstractAuthorizationRule<Task, TaskDao>
{
	public TaskAuthorizationRule(TaskDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Task newResource)
	{
		// allowed status draft | requested for all users
		// task.requester must be organization of current user

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Task existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Task oldResource, Task newResource)
	{
		// allowed status change from draft to requested for remote users
		// update only allowed at status draft for remote users
		// task.requester must be organization of current user or local user
		// only update of tasks with requester = current user allowed for remote users

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Task oldResource)
	{
		// allowed if oldResouce created by current user and status draft

		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
