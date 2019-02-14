package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Task.TaskStatus;

@SearchParameterDefinition(name = SearchTaskStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-status", type = SearchParamType.TOKEN, documentation = "Search by task status")
public class SearchTaskStatus implements SearchParameter
{
	public static final String PARAMETER_NAME = "status";

	private TaskStatus status;

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String status = queryParameters.getFirst(PARAMETER_NAME);

		this.status = status == null || status.isBlank() || !statusValid(status) ? null : TaskStatus.fromCode(status);
	}

	private boolean statusValid(String status)
	{
		// TODO fix control flow by exception
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
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		statement.setString(parameterIndex, status.toCode());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (status != null)
			bundleUri = bundleUri.replaceQueryParam(PARAMETER_NAME, status.toCode());
	}

	@Override
	public void reset()
	{
		// nothing to do
	}
}
