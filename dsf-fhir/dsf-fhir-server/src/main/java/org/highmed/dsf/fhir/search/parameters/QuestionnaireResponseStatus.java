package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractTokenParameter;
import org.highmed.dsf.fhir.search.parameters.basic.TokenSearchType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = QuestionnaireResponseStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/QuestionnaireResponse-status", type = SearchParamType.TOKEN, documentation = "The status of the questionnaire response")
public class QuestionnaireResponseStatus extends AbstractTokenParameter<QuestionnaireResponse>
{
	public static final String PARAMETER_NAME = "status";

	private QuestionnaireResponse.QuestionnaireResponseStatus status;

	public QuestionnaireResponseStatus()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			status = toStatus(valueAndType.codeValue, queryParameters.get(parameterName));
	}

	private QuestionnaireResponse.QuestionnaireResponseStatus toStatus(String status, List<String> parameterValues)
	{
		if (status == null || status.isBlank())
			return null;

		try
		{
			return QuestionnaireResponse.QuestionnaireResponseStatus.fromCode(status);
		}
		catch (FHIRException e)
		{
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
					parameterValues, e));
			return null;
		}
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && status != null;
	}

	@Override
	public String getFilterQuery()
	{
		return "questionnaire_response->>'status' " + (valueAndType.negated ? "<>" : "=") + " ?";
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
		statement.setString(parameterIndex, status.toCode());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME + (valueAndType.negated ? ":not" : ""), status.toCode());
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof QuestionnaireResponse))
			return false;

		if (valueAndType.negated)
			return !Objects.equals(((QuestionnaireResponse) resource).getStatus(), status);
		else
			return Objects.equals(((QuestionnaireResponse) resource).getStatus(), status);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "questionnaire_response->>'status'" + sortDirectionWithSpacePrefix;
	}
}
