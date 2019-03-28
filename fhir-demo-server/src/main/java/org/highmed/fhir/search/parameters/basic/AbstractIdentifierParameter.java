package org.highmed.fhir.search.parameters.basic;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Identifier;

public abstract class AbstractIdentifierParameter<R extends DomainResource> extends AbstractTokenParameter<R>
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
				return resourceColumn + "->'identifier' @> ?::jsonb";
			case CODE_AND_SYSTEM:
				return resourceColumn + "->'identifier' @> ?::jsonb";
			case CODE_AND_NO_SYSTEM_PROPERTY:
				return "(SELECT count(*) FROM jsonb_array_elements(" + resourceColumn
						+ "->'identifier') identifier WHERE identifier->>'value' = ? AND NOT (identifier ?? 'system')) > 0";
			case SYSTEM:
				return resourceColumn + "->'identifier' @> ?::jsonb";
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
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
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

	protected boolean identifierMatches(List<Identifier> identifiers)
	{
		switch (valueAndType.type)
		{
			case CODE:
				return identifiers.stream().anyMatch(i -> Objects.equals(valueAndType.codeValue, i.getValue()));
			case CODE_AND_SYSTEM:
				return identifiers.stream().anyMatch(i -> Objects.equals(valueAndType.codeValue, i.getValue())
						&& Objects.equals(valueAndType.systemValue, i.getSystem()));
			case CODE_AND_NO_SYSTEM_PROPERTY:
				return identifiers.stream().anyMatch(i -> Objects.equals(valueAndType.codeValue, i.getValue())
						&& (i.getSystem() == null || i.getSystem().isBlank()));
			case SYSTEM:
				return identifiers.stream().anyMatch(i -> Objects.equals(valueAndType.systemValue, i.getSystem()));
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
