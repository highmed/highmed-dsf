package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.BasicCrudDao;
import org.hl7.fhir.r4.model.Task;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, BasicCrudDao<Task> crudDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, crudDao);
	}
}
