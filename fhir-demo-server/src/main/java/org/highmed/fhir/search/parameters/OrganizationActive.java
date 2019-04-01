package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractBooleanParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;

@SearchParameterDefinition(name = OrganizationActive.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization-active", type = SearchParamType.TOKEN, documentation = "Is the Organization record active [true|false]")
public class OrganizationActive extends AbstractBooleanParameter<Organization>
{
	public static final String PARAMETER_NAME = "active";

	public OrganizationActive()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "(organization->>'active')::BOOLEAN = ?";
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
		statement.setBoolean(parameterIndex, value);
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Organization))
			return false;

		Organization o = (Organization) resource;

		return o.getActive() == value;
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(organization->>'active')::BOOLEAN " + sortDirectionWithSpacePrefix;
	}
}
