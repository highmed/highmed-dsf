package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStringParameter;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = NamingSystemName.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/NamingSystem-name", type = SearchParamType.STRING, documentation = "A name that this NamingSystem can be identified by")
public class NamingSystemName extends AbstractStringParameter<NamingSystem>
{
	public static final String PARAMETER_NAME = "name";

	public NamingSystemName()
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
				return "lower(naming_system->>'name') LIKE ?";
			case EXACT:
				return "naming_system->>'name' = ?";
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
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Endpoint))
			return false;

		Endpoint e = (Endpoint) resource;

		switch (valueAndType.type)
		{
			case STARTS_WITH:
				return e.getName() != null && e.getName().toLowerCase().startsWith(valueAndType.value.toLowerCase());
			case CONTAINS:
				return e.getName() != null && e.getName().toLowerCase().contains(valueAndType.value.toLowerCase());
			case EXACT:
				return Objects.equals(e.getName(), valueAndType.value);
			default:
				throw notDefined();
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "naming_system->>'name'" + sortDirectionWithSpacePrefix;
	}
}
