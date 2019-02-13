package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = SearchOrganizationNameOrAlias.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-name", type = SearchParamType.STRING, documentation = "A portion of the organization's name or alias")
public class SearchOrganizationNameOrAlias extends AbstractStringSearch implements SearchParameter
{
	public static final String PARAMETER_NAME = "name";

	public SearchOrganizationNameOrAlias()
	{
		super(PARAMETER_NAME);
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
}
