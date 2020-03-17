package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.TaskAuthorizationRule;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;

public class TaskServiceSecure extends AbstractResourceServiceSecure<TaskDao, Task, TaskService> implements TaskService
{
	public TaskServiceSecure(TaskService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, TaskDao taskDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, TaskAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Task.class, taskDao, exceptionHandler,
				parameterConverter, authorizationRule);
	}
}