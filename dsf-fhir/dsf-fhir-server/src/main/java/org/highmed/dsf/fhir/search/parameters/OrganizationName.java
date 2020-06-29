package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStringParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = OrganizationName.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-name", type = SearchParamType.STRING, documentation = "A portion of the organization's name or alias")
public class OrganizationName extends AbstractStringParameter<Organization>
{
	public static final String PARAMETER_NAME = "name";

	public OrganizationName()
	{
		super(PARAMETER_NAME);
	}

	// FIXME property alias is a list
	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
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

	// FIXME property alias is a list
	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		// will be called twice, once with subqueryParameterIndex = 1 and once with subqueryParameterIndex = 2
		switch (valueAndType.type)
		{
			case STARTS_WITH:
				statement.setString(parameterIndex, valueAndType.value.toLowerCase() + "%");
				return;
			case CONTAINS:
				statement.setString(parameterIndex, "%" + valueAndType.value.toLowerCase() + "%");
				return;
			case EXACT:
				statement.setString(parameterIndex, valueAndType.value);
				return;
		}
	}

	// FIXME match against aliases
	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Organization))
			return false;

		Organization o = (Organization) resource;

		switch (valueAndType.type)
		{
			case STARTS_WITH:
				return o.getName() != null && o.getName().toLowerCase().startsWith(valueAndType.value.toLowerCase());
			case CONTAINS:
				return o.getName() != null && o.getName().toLowerCase().contains(valueAndType.value.toLowerCase());
			case EXACT:
				return Objects.equals(o.getName(), valueAndType.value);
			default:
				throw notDefined();
		}
	}

	// FIXME property alias is a list
	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "organization->>'name'" + sortDirectionWithSpacePrefix + ", organization->>'alias'"
				+ sortDirectionWithSpacePrefix;
	}
}
