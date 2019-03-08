package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.SearchQueryIncludeParameter.IncludeParts;
import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractReferenceParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;

@SearchParameterDefinition(name = OrganizationEndpoint.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization.endpoint", type = SearchParamType.REFERENCE, documentation = "Technical endpoints providing access to services operated for the organization")
public class OrganizationEndpoint extends AbstractReferenceParameter<Organization>
{
	private static final String RESOURCE_TYPE_NAME = "Organization";
	public static final String PARAMETER_NAME = "endpoint";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Endpoint";

	public OrganizationEndpoint()
	{
		super(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "? IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') AS reference)";
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
		switch (valueAndType.type)
		{
			case ID:
				statement.setString(parameterIndex, TARGET_RESOURCE_TYPE_NAME + "/" + valueAndType.id);
				break;
			case RESOURCE_NAME_AND_ID:
				statement.setString(parameterIndex, valueAndType.resourceName + "/" + valueAndType.id);
				break;
			case URL:
				statement.setString(parameterIndex, valueAndType.url);
				break;
		}
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Organization))
			return false;

		Organization o = (Organization) resource;

		return o.getEndpoint().stream().map(e -> e.getReference()).anyMatch(ref ->
		{
			switch (valueAndType.type)
			{
				case ID:
					return ref.equals(TARGET_RESOURCE_TYPE_NAME + "/" + valueAndType.id);
				case RESOURCE_NAME_AND_ID:
					return ref.equals(valueAndType.resourceName + "/" + valueAndType.id);
				case URL:
					return ref.equals(valueAndType.url);
				default:
					return false;
			}
		});
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(SELECT string_agg(reference->>'reference', ' ') FROM jsonb_array_elements(organization->'endpoint') AS reference)";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_agg(endpoint) FROM current_endpoints"
					+ " WHERE concat('Endpoint/', endpoint->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') AS reference)"
					+ ") AS endpoints";
		else
			return null;
	}
}
