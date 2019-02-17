package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Task;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = TaskStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-status", type = SearchParamType.TOKEN, documentation = "Search by task status")
public class TaskStatus extends AbstractSearchParameter<Task>
{
	public static final String PARAMETER_NAME = "status";

	private org.hl7.fhir.r4.model.Task.TaskStatus status;

	public TaskStatus()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(MultivaluedMap<String, String> queryParameters)
	{
		status = toStatus(queryParameters.getFirst(PARAMETER_NAME));
	}

	private org.hl7.fhir.r4.model.Task.TaskStatus toStatus(String status)
	{
		if (status == null || status.isBlank())
			return null;

		try
		{
			return org.hl7.fhir.r4.model.Task.TaskStatus.fromCode(status);
		}
		catch (FHIRException e)
		{
			return null;
		}
	}

	@Override
	public boolean isDefined()
	{
		return status != null;
	}

	@Override
	public String getFilterQuery()
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
		bundleUri.replaceQueryParam(PARAMETER_NAME, status.toCode());
	}

	@Override
	public boolean matches(Task resource)
	{
		if (!isDefined())
			throw SearchParameter.notDefined();

		return Objects.equal(resource.getStatus(), status);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "task->>'status'" + sortDirectionWithSpacePrefix;
	}
}
