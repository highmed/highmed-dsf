package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractTokenParameter;
import org.highmed.dsf.fhir.search.parameters.basic.TokenValueAndSearchType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/clinical-identifier", type = SearchParamType.TOKEN, documentation = "Identifies this document reference across multiple systems")
public class DocumentReferenceIdentifier extends AbstractTokenParameter<DocumentReference>
{
	public static final String RESOURCE_COLUMN = "document_reference";
	public static final String PARAMETER_NAME = "identifier";

	public DocumentReferenceIdentifier()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case CODE:
				if (valueAndType.negated)
					return "NOT (document_reference->'identifier' @> ?::jsonb OR document_reference->'masterIdentifier'->>'value' = ?)";
				else
					return "(document_reference->'identifier' @> ?::jsonb OR document_reference->'masterIdentifier'->>'value' = ?)";
			case CODE_AND_SYSTEM:
				if (valueAndType.negated)
					return "NOT (document_reference->'identifier' @> ?::jsonb OR (document_reference->'masterIdentifier'->>'value' = ? AND document_reference->'masterIdentifier'->>'system' = ?))";
				else
					return "(document_reference->'identifier' @> ?::jsonb OR (document_reference->'masterIdentifier'->>'value' = ? AND document_reference->'masterIdentifier'->>'system' = ?))";
			case SYSTEM:
				if (valueAndType.negated)
					return "NOT (document_reference->'identifier' @> ?::jsonb OR document_reference->'masterIdentifier'->>'system' = ?)";
				else
					return "(document_reference->'identifier' @> ?::jsonb OR document_reference->'masterIdentifier'->>'system' = ?)";
			case CODE_AND_NO_SYSTEM_PROPERTY:
				if (valueAndType.negated)
					return "(SELECT count(*) FROM ("
							+ "SELECT identifier FROM jsonb_array_elements(document_reference->'identifier') AS identifier UNION SELECT document_reference->'masterIdentifier') AS document_reference_identifiers "
							+ "WHERE identifier->>'value' <> ? OR (identifier ?? 'system')" + ") > 0";
				else
					return "(SELECT count(*) FROM ("
							+ "SELECT identifier FROM jsonb_array_elements(document_reference->'identifier') AS identifier UNION SELECT document_reference->'masterIdentifier') AS document_reference_identifiers "
							+ "WHERE identifier->>'value' = ? AND NOT (identifier ?? 'system')" + ") > 0";
			default:
				return "";
		}
	}

	@Override
	public int getSqlParameterCount()
	{
		switch (valueAndType.type)
		{
			case CODE:
				return 2;
			case CODE_AND_SYSTEM:
				return 3;
			case SYSTEM:
				return 2;
			case CODE_AND_NO_SYSTEM_PROPERTY:
				return 1;
			default:
				throw notDefined();
		}
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		switch (valueAndType.type)
		{
			case CODE:
				if (subqueryParameterIndex == 1)
					statement.setString(parameterIndex, "[{\"value\": \"" + valueAndType.codeValue + "\"}]");
				else if (subqueryParameterIndex == 2)
					statement.setString(parameterIndex, valueAndType.codeValue);
				return;
			case CODE_AND_SYSTEM:
				if (subqueryParameterIndex == 1)
					statement.setString(parameterIndex, "[{\"value\": \"" + valueAndType.codeValue
							+ "\", \"system\": \"" + valueAndType.systemValue + "\"}]");
				else if (subqueryParameterIndex == 2)
					statement.setString(parameterIndex, valueAndType.codeValue);
				else if (subqueryParameterIndex == 3)
					statement.setString(parameterIndex, valueAndType.systemValue);
				return;
			case SYSTEM:
				if (subqueryParameterIndex == 1)
					statement.setString(parameterIndex, "[{\"system\": \"" + valueAndType.systemValue + "\"}]");
				else if (subqueryParameterIndex == 2)
					statement.setString(parameterIndex, valueAndType.systemValue);
				return;
			case CODE_AND_NO_SYSTEM_PROPERTY:
				statement.setString(parameterIndex, valueAndType.codeValue);
				return;
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof DocumentReference))
			return false;

		DocumentReference d = (DocumentReference) resource;

		return identifierMatches(d.getIdentifier())
				|| (valueAndType.negated ? !identifierMatches(valueAndType, d.getMasterIdentifier())
						: identifierMatches(valueAndType, d.getMasterIdentifier()));
	}

	private boolean identifierMatches(List<Identifier> identifiers)
	{
		return identifiers.stream().anyMatch(
				i -> valueAndType.negated ? !identifierMatches(valueAndType, i) : identifierMatches(valueAndType, i));
	}

	private boolean identifierMatches(TokenValueAndSearchType valueAndType, Identifier identifier)
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
		return "(SELECT string_agg((identifier->>'system')::text || (identifier->>'value')::text, ' ') FROM (SELECT identifier FROM jsonb_array_elements(document_reference->'identifier') identifier "
				+ "UNION SELECT document_reference->'masterIdentifier') AS document_reference_identifier)"
				+ sortDirectionWithSpacePrefix;
	}
}
