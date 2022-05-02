package org.highmed.dsf.fhir.variables;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Target
{
	private final String organizationIdentifierValue;
	private final String endpointIdentifierValue;
	private final String endpointUrl;
	private final String correlationKey;

	@JsonCreator
	private Target(
			@JsonProperty("organizationIdentifierValue") @JsonAlias("targetOrganizationIdentifierValue") String organizationIdentifierValue,
			@JsonProperty("endpointIdentifierValue") String endpointIdentifierValue,
			@JsonProperty("endpointUrl") @JsonAlias("targetEndpointUrl") String endpointUrl,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.organizationIdentifierValue = organizationIdentifierValue;
		this.endpointIdentifierValue = endpointIdentifierValue;
		this.endpointUrl = endpointUrl;
		this.correlationKey = correlationKey;
	}

	public static Target createUniDirectionalTarget(String targetOrganizationIdentifierValue,
			String targetEndpointIdentifierValue, String targetEndpointUrl)
	{
		return new Target(targetOrganizationIdentifierValue, targetEndpointIdentifierValue, targetEndpointUrl, null);
	}

	@Deprecated
	public static Target createUniDirectionalTarget(String targetOrganizationIdentifierValue, String targetEndpointUrl)
	{
		return createUniDirectionalTarget(targetOrganizationIdentifierValue, null, targetEndpointUrl);
	}

	public static Target createBiDirectionalTarget(String targetOrganizationIdentifierValue,
			String targetEndpointIdentifierValue, String targetEndpointUrl, String correlationKey)
	{
		return new Target(targetOrganizationIdentifierValue, targetEndpointIdentifierValue, targetEndpointUrl,
				correlationKey);
	}

	@Deprecated
	public static Target createBiDirectionalTarget(String targetOrganizationIdentifierValue, String targetEndpointUrl,
			String correlationKey)
	{
		return createBiDirectionalTarget(targetOrganizationIdentifierValue, null, targetEndpointUrl, correlationKey);
	}

	/**
	 * @return the target Organizations identifier value
	 * @deprecated use {@link #getOrganizationIdentifierValue()}
	 */
	@Deprecated
	public String getTargetOrganizationIdentifierValue()
	{
		return getOrganizationIdentifierValue();
	}

	/**
	 * @return the target Organizations identifier value
	 */
	@JsonProperty("organizationIdentifierValue")
	public String getOrganizationIdentifierValue()
	{
		return organizationIdentifierValue;
	}

	/**
	 * @return the target Endpoints identifier value
	 */
	@JsonProperty("endpointIdentifierValue")
	public String getEndpointIdentifierValue()
	{
		return endpointIdentifierValue;
	}

	/**
	 * @return the target Endpoints url
	 * @deprecated use {@link #getEndpointUrl()}
	 */
	@Deprecated
	public String getTargetEndpointUrl()
	{
		return getEndpointUrl();
	}

	/**
	 * @return the target Endpoints url
	 */
	@JsonProperty("endpointUrl")
	public String getEndpointUrl()
	{
		return endpointUrl;
	}

	/**
	 * @return the correlation key used in bidirectional communications
	 */
	@JsonProperty("correlationKey")
	public String getCorrelationKey()
	{
		return correlationKey;
	}
}
