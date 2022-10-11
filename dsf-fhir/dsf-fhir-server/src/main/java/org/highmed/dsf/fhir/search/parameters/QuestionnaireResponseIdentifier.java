package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-identifier", type = SearchParamType.TOKEN, documentation = "The unique identifier for the questionnaire response")
public class QuestionnaireResponseIdentifier extends AbstractIdentifierParameter<QuestionnaireResponse>
{
	public static final String RESOURCE_COLUMN = "questionnaire_response";

	public QuestionnaireResponseIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case CODE:
			case CODE_AND_SYSTEM:
			case SYSTEM:
				return "questionnaire_response->'identifier' " + (valueAndType.negated ? "<>" : "=") + " ?::jsonb";
			case CODE_AND_NO_SYSTEM_PROPERTY:
				if (valueAndType.negated)
					return "questionnaire_response->'identifier'->>'value' <> ? OR (questionnaire_response->'identifier' ?? 'system')";
				else
					return "questionnaire_response->'identifier'->>'value' = ? AND NOT (questionnaire_response->'identifier' ?? 'system')";
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
		return "(questionnaire_response->'identifier'->>'system')::text || (questionnaire_response->'identifier'->>'value')::text"
				+ sortDirectionWithSpacePrefix;
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof QuestionnaireResponse))
			return false;

		QuestionnaireResponse qr = (QuestionnaireResponse) resource;

		return identifierMatches(qr.getIdentifier());
	}
}
