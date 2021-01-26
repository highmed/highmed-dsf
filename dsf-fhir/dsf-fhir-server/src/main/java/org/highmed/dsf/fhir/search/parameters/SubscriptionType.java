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
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;

@SearchParameterDefinition(name = SubscriptionType.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Subscription-type", type = SearchParamType.TOKEN, documentation = "The type of channel for the sent notifications")
public class SubscriptionType extends AbstractTokenParameter<Subscription>
{
	public static final String PARAMETER_NAME = "type";

	private org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType channelType;

	public SubscriptionType()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			channelType = toChannelType(valueAndType.codeValue);
	}

	private org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType toChannelType(String status)
	{
		if (status == null || status.isBlank())
			return null;

		try
		{
			return org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType.fromCode(status);
		}
		catch (FHIRException e)
		{
			return null;
		}
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && channelType != null;
	}

	@Override
	public String getFilterQuery()
	{
		return "subscription->'channel'->>'type' " + (valueAndType.negated ? "<>" : "=") + " ?";
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
		statement.setString(parameterIndex, channelType.toCode());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME + (valueAndType.negated ? ":not" : ""), channelType.toCode());
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Subscription))
			return false;

		if (valueAndType.negated)
			return !Objects.equals(((Subscription) resource).getChannel().getType(), channelType);
		else
			return Objects.equals(((Subscription) resource).getChannel().getType(), channelType);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "subscription->'channel'->>'type'" + sortDirectionWithSpacePrefix;
	}
}
