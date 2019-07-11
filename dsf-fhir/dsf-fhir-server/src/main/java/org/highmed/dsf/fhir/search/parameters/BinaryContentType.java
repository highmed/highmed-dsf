package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractTokenParameter;
import org.highmed.dsf.fhir.search.parameters.basic.TokenSearchType;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;

import javax.ws.rs.core.UriBuilder;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SearchParameterDefinition(name = BinaryContentType.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Binary-contentType", type = SearchParamType.TOKEN, documentation = "The MIME type of the actual binary content")
public class BinaryContentType extends AbstractTokenParameter<Binary>
{
	public static final String PARAMETER_NAME = "contentType";

	private CodeType contentType;

	public BinaryContentType()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			contentType = toContentType(valueAndType.codeValue);
	}

	private CodeType toContentType(String contentType)
	{
		if (contentType == null || contentType.isBlank())
			return null;

		return new CodeType(contentType);
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && contentType != null;
	}

	@Override
	public String getFilterQuery()
	{
		return "binary_data->>'contentType' = ?";
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
		statement.setString(parameterIndex, contentType.getSystem());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME, contentType.getSystem());
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Binary))
			return false;

		return ((Binary) resource).getContentType().equals(contentType.getSystem());
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "binary_data->>'contentType'" + sortDirectionWithSpacePrefix;
	}
}
