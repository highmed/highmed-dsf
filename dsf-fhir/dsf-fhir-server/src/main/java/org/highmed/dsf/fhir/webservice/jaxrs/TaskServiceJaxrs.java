package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

@Path(TaskServiceJaxrs.PATH)
public class TaskServiceJaxrs extends AbstractResourceServiceJaxrs<Task, TaskService> implements TaskService
{
	public static final String PATH = "Task";

	public TaskServiceJaxrs(TaskService delegate)
	{
		super(delegate);
	}
}
