package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAuthorizationRule extends AbstractAuthorizationRule<Task, TaskDao>
{
	private static final Logger logger = LoggerFactory.getLogger(TaskAuthorizationRule.class);

	public TaskAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver)
	{
		super(Task.class, daoProvider, serverBase, referenceResolver);
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
		logger.info("Search of Task authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
