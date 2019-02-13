package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = SearchId.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-id", type = SearchParamType.TOKEN, documentation = "Logical id of this artifact")
public class SearchId implements SearchParameter
{
	public static final String PARAMETER_NAME = "_id";

	private final String resourceIdColumn;
	private String id;

	public SearchId(String resourceIdColumn)
	{
		this.resourceIdColumn = resourceIdColumn;
	}

	@Override
	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String id = queryParameters.getFirst(PARAMETER_NAME);
		this.id = id == null || id.isBlank() ? null : id;
	}

	@Override
	public boolean isDefined()
	{
		return id != null;
	}

	@Override
	public String getSubquery()
	{
		return resourceIdColumn + " = ?";
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
		statement.setString(parameterIndex, id);
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (id != null)
			bundleUri.replaceQueryParam(PARAMETER_NAME, id);
	}
}
