package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = SearchOrganizationNameOrAlias.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-name", type = SearchParamType.STRING, documentation = "A portion of the organization's name or alias")
public class SearchOrganizationNameOrAlias implements SearchParameter
{
	public static final String PARAMETER_NAME = "name";

	private String name;

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String name = queryParameters.getFirst(PARAMETER_NAME);

		this.name = name == null || name.isBlank() ? null : name;
	}

	@Override
	public boolean isDefined()
	{
		return name != null;
	}

	@Override
	public String getSubquery()
	{
		return "(lower(organization->>'name') LIKE ? OR lower(organization->>'alias') LIKE ?)";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 2;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		// will be called twice, once with subqueryParameterIndex = 1 and once with subqueryParameterIndex = 2
		statement.setString(parameterIndex, "%" + name.toLowerCase() + "%");
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (name != null)
			bundleUri = bundleUri.replaceQueryParam("name", name);
	}
}
