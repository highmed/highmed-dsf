package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.TaskDaoJdbc;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;

public class TaskDaoTest extends AbstractResourceDaoTest<Task, TaskDao>
{
	private static final TaskStatus status = TaskStatus.REQUESTED;
	private static final String description = "Demo Task Description";

	public TaskDaoTest()
	{
		super(Task.class);
	}

	@Override
	protected TaskDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new TaskDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Task createResource()
	{
		Task task = new Task();
		task.setStatus(status);
		return task;
	}

	@Override
	protected void checkCreated(Task resource)
	{
		assertEquals(status, resource.getStatus());
	}

	@Override
	protected Task updateResource(Task resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(Task resource)
	{
		assertEquals(description, resource.getDescription());
	}
}
