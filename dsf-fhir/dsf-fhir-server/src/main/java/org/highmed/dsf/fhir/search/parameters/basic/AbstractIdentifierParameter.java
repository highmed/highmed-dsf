package org.highmed.dsf.fhir.search.parameters.basic;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractIdentifierParameter<R extends Resource> extends AbstractTokenParameter<R>
{
	public static final String PARAMETER_NAME = "identifier";

	private final String resourceColumn;

	public AbstractIdentifierParameter(String resourceColumn)
	{
		super(PARAMETER_NAME);

		this.resourceColumn = resourceColumn;
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case CODE:
			case CODE_AND_SYSTEM:
			case SYSTEM:
				if (valueAndType.negated)
					return "NOT (" + resourceColumn + "->'identifier' @> ?::jsonb)";
				else
					return resourceColumn + "->'identifier' @> ?::jsonb";
			case CODE_AND_NO_SYSTEM_PROPERTY:
				if (valueAndType.negated)
					return "(SELECT count(*) FROM jsonb_array_elements(" + resourceColumn
							+ "->'identifier') identifier WHERE identifier->>'value' <> ? OR (identifier ?? 'system')) > 0";
				else
					return "(SELECT count(*) FROM jsonb_array_elements(" + resourceColumn
							+ "->'identifier') identifier WHERE identifier->>'value' = ? AND NOT (identifier ?? 'system')) > 0";
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
				statement.setString(parameterIndex, "[{\"value\": \"" + valueAndType.codeValue + "\"}]");
				return;
			case CODE_AND_SYSTEM:
				statement.setString(parameterIndex, "[{\"value\": \"" + valueAndType.codeValue + "\", \"system\": \""
						+ valueAndType.systemValue + "\"}]");
				return;
			case CODE_AND_NO_SYSTEM_PROPERTY:
				statement.setString(parameterIndex, valueAndType.codeValue);
				return;
			case SYSTEM:
				statement.setString(parameterIndex, "[{\"system\": \"" + valueAndType.systemValue + "\"}]");
				return;
		}
	}

	protected final boolean identifierMatches(List<Identifier> identifiers)
	{
		return identifiers.stream().anyMatch(
				i -> valueAndType.negated ? !identifierMatches(valueAndType, i) : identifierMatches(valueAndType, i));
	}

	public static boolean identifierMatches(TokenValueAndSearchType valueAndType, Identifier identifier)
	{
		switch (valueAndType.type)
		{
			case CODE:
				return Objects.equals(valueAndType.codeValue, identifier.getValue());
			case CODE_AND_SYSTEM:
				return Objects.equals(valueAndType.codeValue, identifier.getValue())
						&& Objects.equals(valueAndType.systemValue, identifier.getSystem());
			case CODE_AND_NO_SYSTEM_PROPERTY:
				return Objects.equals(valueAndType.codeValue, identifier.getValue())
						&& (identifier.getSystem() == null || identifier.getSystem().isBlank());
			case SYSTEM:
				return Objects.equals(valueAndType.systemValue, identifier.getSystem());
			default:
				return false;
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(SELECT string_agg((identifier->>'system')::text || (identifier->>'value')::text, ' ') FROM jsonb_array_elements("
				+ resourceColumn + "->'identifier') identifier)" + sortDirectionWithSpacePrefix;
	}
}
