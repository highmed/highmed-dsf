package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public class SearchTaskStatus implements SearchQuery
{
	private final TaskStatus status;

	public SearchTaskStatus(String status)
	{
		this.status = status == null || status.isBlank() || !statusValid(status) ? null : TaskStatus.fromCode(status);
	}

	private boolean statusValid(String status)
	{
		// FIXME control flow by exception
		try
		{
			TaskStatus.fromCode(status);
			return true;
		}
		catch (FHIRException e)
		{
			return false;
		}
	}

	@Override
	public boolean isDefined()
	{
		return status != null;
	}

	@Override
	public String getSubquery()
	{
		return "task->>'status' = ?";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		statement.setString(parameterIndex, status.toCode());
	}
}
