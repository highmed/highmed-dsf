package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.search.parameters.TaskAuthoredOn;
import org.highmed.dsf.fhir.search.parameters.TaskIdentifier;
import org.highmed.dsf.fhir.search.parameters.TaskModified;
import org.highmed.dsf.fhir.search.parameters.TaskRequester;
import org.highmed.dsf.fhir.search.parameters.TaskStatus;
import org.highmed.dsf.fhir.search.parameters.user.TaskUserFilter;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class TaskDaoJdbc extends AbstractResourceDaoJdbc<Task> implements TaskDao
{
	public TaskDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Task.class, "tasks", "task", "task_id", TaskUserFilter::new,
				with(TaskAuthoredOn::new, TaskIdentifier::new, TaskModified::new, TaskRequester::new, TaskStatus::new),
				with());
	}

	@Override
	protected Task copy(Task resource)
	{
		return resource.copy();
	}
}
