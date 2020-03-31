package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractCanonicalUrlParameter;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = EndpointAddress.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Endpoint-address", type = SearchParamType.URI, documentation = "The address (url) of the endpoint")
public class EndpointAddress extends AbstractCanonicalUrlParameter<Endpoint>
{
	public static final String PARAMETER_NAME = "address";

	public EndpointAddress()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case PRECISE:
				return "endpoint->>'address' = ?";
			case BELOW:
				return "endpoint->>'address' LIKE ?";
			default:
				return "";
		}
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		switch (valueAndType.type)
		{
			case PRECISE:
				statement.setString(parameterIndex, valueAndType.url);
				return;
			case BELOW:
				statement.setString(parameterIndex, valueAndType.url + "%");
				return;
			default:
				return;
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Endpoint))
			return false;

		Endpoint endpoint = (Endpoint) resource;

		switch (valueAndType.type)
		{
			case PRECISE:
				return Objects.equals(endpoint.getAddress(), valueAndType.url);
			case BELOW:
				return endpoint.getAddress() != null && endpoint.getAddress().startsWith(valueAndType.url);
			default:
				throw notDefined();
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "endpoint->>'address'" + sortDirectionWithSpacePrefix;
	}
}
