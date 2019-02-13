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

	private enum StringSearchType
	{
		STARTS_WITH, EXACT, CONTAINS
	}

	private String value;
	private StringSearchType type;

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String startsWith = queryParameters.getFirst(PARAMETER_NAME);
		if (startsWith != null && !startsWith.isBlank())
		{
			this.value = startsWith;
			this.type = StringSearchType.STARTS_WITH;
		}
		String exact = queryParameters.getFirst(PARAMETER_NAME + ":exact");
		if (exact != null && !exact.isBlank())
		{
			this.value = exact;
			this.type = StringSearchType.EXACT;
		}
		String contains = queryParameters.getFirst(PARAMETER_NAME + ":contains");
		if (contains != null && !contains.isBlank())
		{
			this.value = contains;
			this.type = StringSearchType.CONTAINS;
		}
	}

	@Override
	public boolean isDefined()
	{
		return value != null && type != null;
	}

	@Override
	public String getSubquery()
	{
		switch (type)
		{
			case STARTS_WITH:
			case CONTAINS:
				return "(lower(organization->>'name') LIKE ? OR lower(organization->>'alias') LIKE ?)";
			case EXACT:
				return "(organization->>'name' = ? OR organization->>'alias' = ?)";
			default:
				return "";
		}
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
		switch (type)
		{
			case STARTS_WITH:
				statement.setString(parameterIndex, value.toLowerCase() + "%");
				return;
			case CONTAINS:
				statement.setString(parameterIndex, "%" + value.toLowerCase() + "%");
				return;
			case EXACT:
				statement.setString(parameterIndex, value);
				return;
		}
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (value != null && type != null)
			switch (type)
			{
				case STARTS_WITH:
					bundleUri = bundleUri.replaceQueryParam(PARAMETER_NAME, value);
					return;
				case CONTAINS:
					bundleUri = bundleUri.replaceQueryParam(PARAMETER_NAME + ":contains", value);
					return;
				case EXACT:
					bundleUri = bundleUri.replaceQueryParam(PARAMETER_NAME + ":exact", value);
					return;
			}
	}
}
