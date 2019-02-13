package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.IdType;

@SearchParameterDefinition(name = SearchTaskRequester.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-requester", type = SearchParamType.REFERENCE, documentation = "Search by task requester")
public class SearchTaskRequester implements SearchParameter
{
	public static final String PARAMETER_NAME = "requester";

	private IdType requester;

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String requester = queryParameters.getFirst(PARAMETER_NAME);

		this.requester = requester == null || requester.isBlank() ? null : new IdType(requester);
	}

	@Override
	public boolean isDefined()
	{
		return requester != null;
	}

	@Override
	public String getSubquery()
	{
		return "task->'requester'->>'reference' " + (requester.hasVersionIdPart() ? "=" : "LIKE") + " ?";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		statement.setString(parameterIndex, requester.getValue() + (requester.hasVersionIdPart() ? "" : "%"));
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (requester != null)
			bundleUri = bundleUri.replaceQueryParam("requester", requester);
	}
}
