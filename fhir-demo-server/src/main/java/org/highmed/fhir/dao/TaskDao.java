package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class TaskDao extends AbstractDao<Task>
{
	private static final Logger logger = LoggerFactory.getLogger(TaskDao.class);

	public TaskDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Task.class, "tasks", "task", "task_id");
	}

	@Override
	protected Task copy(Task resource)
	{
		return resource.copy();
	}

	public List<Task> getTaskBy(IdType requester) throws SQLException
	{
		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection
						.prepareStatement("SELECT task FROM tasks WHERE task->'requester'->>'reference' "
								+ (requester.hasVersionIdPart() ? "=" : "LIKE") + " ?"))
		{
			statement.setString(1, requester.getValue() + (requester.hasVersionIdPart() ? "" : "%"));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<Task> tasks = new ArrayList<>();
				while (result.next())
					tasks.add(getResource(result, 1));
				return tasks;
			}
		}
	}
}
