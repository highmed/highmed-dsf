package org.highmed.dsf.fhir.variables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Target
{
	private static final Logger logger = LoggerFactory.getLogger(Target.class);

	private final String targetOrganizationIdentifierValue;
	private final String correlationKey;

	@JsonCreator
	private Target(
			@JsonProperty("targetOrganizationIdentifierValue")
					String targetOrganizationIdentifierValue,
			@JsonProperty("correlationKey")
					String correlationKey)
	{
		this.targetOrganizationIdentifierValue = targetOrganizationIdentifierValue;
		this.correlationKey = correlationKey;
	}

	public static Target createUniDirectionalTarget(String targetOrganizationIdentifierValue)
	{
		return new Target(targetOrganizationIdentifierValue, null);
	}

	public static Target createBiDirectionalTarget(String targetOrganizationIdentifierValue, String correlationKey)
	{
		return new Target(targetOrganizationIdentifierValue, correlationKey);
	}

	public String getTargetOrganizationIdentifierValue()
	{
		return targetOrganizationIdentifierValue;
	}

	public String getCorrelationKey()
	{
		if (correlationKey == null)
			logger.debug("SingleInstanceTargets do not have a correlation key");

		return correlationKey;
	}
}
