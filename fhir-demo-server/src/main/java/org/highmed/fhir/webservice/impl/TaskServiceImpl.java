package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceImpl extends AbstractServiceImpl<TaskDao, Task> implements TaskService
{
	public TaskServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount, TaskDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator<Task> eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
