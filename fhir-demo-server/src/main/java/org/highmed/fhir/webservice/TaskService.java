package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.dao.search.SearchTaskRequester;
import org.highmed.fhir.dao.search.SearchTaskStatus;
import org.hl7.fhir.r4.model.Task;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<TaskDao, Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, int defaultPageCount, TaskDao taskDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, taskDao,
				withSearchParameters(new SearchTaskRequester(), new SearchTaskStatus()));
	}
}
