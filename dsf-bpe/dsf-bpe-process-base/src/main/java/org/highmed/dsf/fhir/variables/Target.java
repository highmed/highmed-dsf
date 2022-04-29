package org.highmed.dsf.fhir.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Target
{
	private final String targetOrganizationIdentifierValue;
	private final String targetEndpointIdentifierValue;
	private final String targetEndpointUrl;
	private final String correlationKey;

	@JsonCreator
	private Target(@JsonProperty("targetOrganizationIdentifierValue") String targetOrganizationIdentifierValue,
			@JsonProperty("targetEndpointIdentifierValue") String targetEndpointIdentifierValue,
			@JsonProperty("targetEndpointUrl") String targetEndpointUrl,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.targetOrganizationIdentifierValue = targetOrganizationIdentifierValue;
		this.targetEndpointIdentifierValue = targetEndpointIdentifierValue;
		this.targetEndpointUrl = targetEndpointUrl;
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

	@JsonProperty("targetOrganizationIdentifierValue")
	public String getTargetOrganizationIdentifierValue()
	{
		return targetOrganizationIdentifierValue;
	}

	@JsonProperty("targetEndpointIdentifierValue")
	public String getTargetEndpointIdentifierValue()
	{
		return targetEndpointIdentifierValue;
	}

	@JsonProperty("targetEndpointUrl")
	public String getTargetEndpointUrl()
	{
		return targetEndpointUrl;
	}

	@JsonProperty("correlationKey")
	public String getCorrelationKey()
	{
		return correlationKey;
	}
}
