package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.search.parameters.TaskRequester;
import org.highmed.fhir.search.parameters.TaskStatus;
import org.highmed.fhir.service.ResourceValidator;
import org.hl7.fhir.r4.model.Task;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<TaskDao, Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, int defaultPageCount, TaskDao taskDao, ResourceValidator validator,
			EventManager eventManager)
	{
		super(serverBase, defaultPageCount, Task.class, taskDao, validator, eventManager, TaskRequester::new,
				TaskStatus::new);
	}
}
