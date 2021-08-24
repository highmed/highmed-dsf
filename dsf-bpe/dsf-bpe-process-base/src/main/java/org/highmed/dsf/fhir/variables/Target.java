package org.highmed.dsf.fhir.variables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Target
{
	private static final Logger logger = LoggerFactory.getLogger(Target.class);

	private final String targetOrganizationIdentifierValue;
	private final String targetEndpointUrl;
	private final String correlationKey;

	@JsonCreator
	private Target(@JsonProperty("targetOrganizationIdentifierValue") String targetOrganizationIdentifierValue,
			@JsonProperty("targetEndpointUrl") String targetEndpointUrl,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.targetOrganizationIdentifierValue = targetOrganizationIdentifierValue;
		this.targetEndpointUrl = targetEndpointUrl;
		this.correlationKey = correlationKey;
	}

	public static Target createUniDirectionalTarget(String targetOrganizationIdentifierValue, String targetEndpointUrl)
	{
		return new Target(targetOrganizationIdentifierValue, targetEndpointUrl, null);
	}

	public static Target createBiDirectionalTarget(String targetOrganizationIdentifierValue, String targetEndpointUrl,
			String correlationKey)
	{
		return new Target(targetOrganizationIdentifierValue, targetEndpointUrl, correlationKey);
	}

	public String getTargetOrganizationIdentifierValue()
	{
		return targetOrganizationIdentifierValue;
	}

	public String getTargetEndpointUrl()
	{
		return targetEndpointUrl;
	}

	public String getCorrelationKey()
	{
		if (correlationKey == null)
			logger.debug("SingleInstanceTargets do not have a correlation key");

		return correlationKey;
	}
}
