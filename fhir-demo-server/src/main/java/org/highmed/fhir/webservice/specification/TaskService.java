package org.highmed.fhir.webservice.specification;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.Task;

@Path(TaskService.PATH)
public interface TaskService extends BasicService<Task>
{
	final String PATH = "Task";
}
