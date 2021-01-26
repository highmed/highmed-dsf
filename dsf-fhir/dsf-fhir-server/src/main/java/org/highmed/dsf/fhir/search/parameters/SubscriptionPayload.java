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
import org.highmed.dsf.fhir.search.parameters.basic.AbstractTokenParameter;
import org.highmed.dsf.fhir.search.parameters.basic.TokenSearchType;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;

@SearchParameterDefinition(name = SubscriptionPayload.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Subscription-payload", type = SearchParamType.TOKEN, documentation = "The mime-type of the notification payload")
public class SubscriptionPayload extends AbstractTokenParameter<Subscription>
{
	public static final String PARAMETER_NAME = "payload";

	private String payloadMimeType;

	public SubscriptionPayload()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			payloadMimeType = valueAndType.codeValue;
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && payloadMimeType != null;
	}

	@Override
	public String getFilterQuery()
	{
		return "subscription->'channel'->>'payload' " + (valueAndType.negated ? "<>" : "=") + " ?";
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
		statement.setString(parameterIndex, payloadMimeType);
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME + (valueAndType.negated ? ":not" : ""), payloadMimeType);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Subscription))
			return false;

		if (valueAndType.negated)
			return !Objects.equals(((Subscription) resource).getChannel().getPayload(), payloadMimeType);
		else
			return Objects.equals(((Subscription) resource).getChannel().getPayload(), payloadMimeType);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "subscription->'channel'->>'payload'" + sortDirectionWithSpacePrefix;
	}
}
