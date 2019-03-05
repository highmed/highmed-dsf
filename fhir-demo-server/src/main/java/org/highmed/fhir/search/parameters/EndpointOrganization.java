package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractReferenceParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = EndpointOrganization.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Endpoint.managingOrganization", type = SearchParamType.REFERENCE, documentation = "The organization that is managing the endpoint")
public class EndpointOrganization extends AbstractReferenceParameter<Endpoint>
{
	public static final String PARAMETER_NAME = "organization";
	private static final String RESOURCE_NAME = "Organization";

	public EndpointOrganization()
	{
		super(PARAMETER_NAME, RESOURCE_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		return "endpoint->'managingOrganization'->>'reference' = ?";
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
				statement.setString(parameterIndex, RESOURCE_NAME + "/" + valueAndType.id);
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

		if (!(resource instanceof Endpoint))
			return false;

		Endpoint e = (Endpoint) resource;

		String ref = e.getManagingOrganization().getReference();
		switch (valueAndType.type)
		{
			case ID:
				return ref.equals(RESOURCE_NAME + "/" + valueAndType.id);
			case RESOURCE_NAME_AND_ID:
				return ref.equals(valueAndType.resourceName + "/" + valueAndType.id);
			case URL:
				return ref.equals(valueAndType.url);
			default:
				return false;
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "endpoint->'managingOrganization'->>'reference'";
	}
}
