package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.SearchParameter;
import org.highmed.fhir.webservice.search.AbstractStringParameter;
import org.highmed.fhir.webservice.search.WsSearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = OrganizationName.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-name", type = SearchParamType.STRING, documentation = "A portion of the organization's name or alias")
public class OrganizationName extends AbstractStringParameter implements SearchParameter<Organization>
{
	public static final String PARAMETER_NAME = "name";

	public OrganizationName()
	{
		super(PARAMETER_NAME);
	}

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

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
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

	@Override
	public boolean matches(Organization resource)
	{
		switch (valueAndType.type)
		{
			case STARTS_WITH:
				return resource.getName() != null
						&& resource.getName().toLowerCase().startsWith(valueAndType.value.toLowerCase());
			case CONTAINS:
				return resource.getName() != null
						&& resource.getName().toLowerCase().contains(valueAndType.value.toLowerCase());
			case EXACT:
				return Objects.equal(resource.getName(), valueAndType.value);
			default:
				return false;
		}
	}
}
