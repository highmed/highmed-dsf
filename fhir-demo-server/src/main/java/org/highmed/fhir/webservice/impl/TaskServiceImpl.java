package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceImpl extends AbstractServiceImpl<TaskDao, Task> implements TaskService
{
	public TaskServiceImpl(String serverBase, int defaultPageCount, TaskDao taskDao, ResourceValidator validator,
			EventManager eventManager, ServiceHelperImpl<Task> serviceHelper)
	{
		super(serverBase, defaultPageCount, taskDao, validator, eventManager, serviceHelper);
	}
}
