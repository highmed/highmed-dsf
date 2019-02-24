package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;

@SearchParameterDefinition(name = TaskRequester.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-requester", type = SearchParamType.REFERENCE, documentation = "Search by task requester")
public class TaskRequester extends AbstractSearchParameter<Task>
{
	public static final String PARAMETER_NAME = "requester";

	private IdType requester;

	public TaskRequester()
	{
		super(Task.class, PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		requester = toIdType(getFirst(queryParameters, PARAMETER_NAME));
	}

	private IdType toIdType(String requester)
	{
		return requester == null || requester.isBlank() ? null : new IdType(requester);
	}

	@Override
	public boolean isDefined()
	{
		return requester != null;
	}

	@Override
	public String getFilterQuery()
	{
		return "task->'requester'->>'reference' " + (requester.hasVersionIdPart() ? "=" : "LIKE") + " ?";
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
		statement.setString(parameterIndex, requester.getValue() + (requester.hasVersionIdPart() ? "" : "%"));
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME, requester);
	}

	@Override
	public boolean matches(Task resource)
	{
		if (!isDefined())
			throw SearchParameter.notDefined();

		if (requester.hasVersionIdPart())
			return Objects.equals(resource.getRequester().getIdElement().getValue(),
					requester.getIdElement().getValue());
		else if (resource.getRequester().getIdElement().getValue() != null)
			return resource.getRequester().getIdElement().getValue().startsWith(requester.getIdElement().getValue());
		else
			return false;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "task->'requester'->>'reference'" + sortDirectionWithSpacePrefix;
	}
}
