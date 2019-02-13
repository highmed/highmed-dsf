package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.highmed.fhir.dao.search.SearchTaskRequester;
import org.highmed.fhir.dao.search.SearchTaskStatus;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class TaskDao extends AbstractDao<Task>
{
	public TaskDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Task.class, "tasks", "task", "task_id");
	}

	@Override
	protected Task copy(Task resource)
	{
		return resource.copy();
	}

	public PartialResult<Task> search(String requester, String status, int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count)
				.with(new SearchTaskRequester(requester), new SearchTaskStatus(status)).build());
	}
}
