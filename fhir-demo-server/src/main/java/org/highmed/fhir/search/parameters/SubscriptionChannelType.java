package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.parameters.basic.AbstractTokenParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Subscription;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = SubscriptionChannelType.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Subscription.channel.type", type = SearchParamType.TOKEN, documentation = "The type of channel for the sent notifications")
public class SubscriptionChannelType extends AbstractTokenParameter<Subscription>
{
	public static final String PARAMETER_NAME = "type";

	private org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType channelType;

	public SubscriptionChannelType()
	{
		super(PARAMETER_NAME);
	}

	@Override
	protected void configureSearchParameter(MultivaluedMap<String, String> queryParameters)
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
		return "subscription->'channel'->>'type' = ?";
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
		statement.setString(parameterIndex, channelType.toCode());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME, channelType.toCode());
	}

	@Override
	public boolean matches(Subscription resource)
	{
		if (!isDefined())
			throw SearchParameter.notDefined();

		return Objects.equal(resource.getChannel().getType(), channelType);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "subscription->'channel'->>'type'" + sortDirectionWithSpacePrefix;
	}
}
