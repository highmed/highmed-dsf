package org.highmed.dsf.fhir.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceTarget
{
	private final String targetOrganizationIdentifierValue;
	private final String correlationKey;

	@JsonCreator
	public MultiInstanceTarget(
			@JsonProperty("targetOrganizationIdentifierValue") String targetOrganizationIdentifierValue,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.targetOrganizationIdentifierValue = targetOrganizationIdentifierValue;
		this.correlationKey = correlationKey;
	}

	public String getTargetOrganizationIdentifierValue()
	{
		return targetOrganizationIdentifierValue;
	}

	public String getCorrelationKey()
	{
		return correlationKey;
	}
}
