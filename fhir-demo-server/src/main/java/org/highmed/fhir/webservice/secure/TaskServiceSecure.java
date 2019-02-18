package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceSecure extends AbstractServiceSecure<Task, TaskService> implements TaskService
{
	public TaskServiceSecure(TaskService delegate)
	{
		super(delegate);
	}
}
