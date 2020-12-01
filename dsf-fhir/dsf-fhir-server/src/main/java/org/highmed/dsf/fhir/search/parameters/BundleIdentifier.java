package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractTokenParameter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = BundleIdentifier.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Bundle-identifier", type = SearchParamType.TOKEN, documentation = "Persistent identifier for the bundle")
public class BundleIdentifier extends AbstractTokenParameter<Bundle>
{
	public static final String PARAMETER_NAME = "identifier";

	public BundleIdentifier()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case CODE:
			case CODE_AND_SYSTEM:
			case SYSTEM:
				return "bundle->'identifier' " + (valueAndType.negated ? "<>" : "=") + " ?::jsonb";
			case CODE_AND_NO_SYSTEM_PROPERTY:
				if (valueAndType.negated)
					return "bundle->'identifier'->>'value' <> ? OR (bundle->'identifier' ?? 'system')";
				else
					return "bundle->'identifier'->>'value' = ? AND NOT (bundle->'identifier' ?? 'system')";
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
			case CODE:
				statement.setString(parameterIndex, "{\"value\": \"" + valueAndType.codeValue + "\"}");
				return;
			case CODE_AND_SYSTEM:
				statement.setString(parameterIndex, "{\"value\": \"" + valueAndType.codeValue + "\", \"system\": \""
						+ valueAndType.systemValue + "\"}");
				return;
			case CODE_AND_NO_SYSTEM_PROPERTY:
				statement.setString(parameterIndex, valueAndType.codeValue);
				return;
			case SYSTEM:
				statement.setString(parameterIndex, "{\"system\": \"" + valueAndType.systemValue + "\"}");
				return;
		}
	}

	private boolean identifierMatches(Identifier identifier)
	{
		if (valueAndType.negated)
			return !AbstractIdentifierParameter.identifierMatches(valueAndType, identifier);
		else
			return AbstractIdentifierParameter.identifierMatches(valueAndType, identifier);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(bundle->'identifier'->>'system')::text || (bundle->'identifier'->>'value')::text"
				+ sortDirectionWithSpacePrefix;
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Bundle))
			return false;

		Bundle e = (Bundle) resource;

		return identifierMatches(e.getIdentifier());
	}
}
