package org.highmed.fhir.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceTarget
{
	private final String targetOrganizationId;
	private final String correlationKey;

	@JsonCreator
	public MultiInstanceTarget(@JsonProperty("targetOrganizationId") String targetOrganizationId,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.targetOrganizationId = targetOrganizationId;
		this.correlationKey = correlationKey;
	}

	public String getTargetOrganizationId()
	{
		return targetOrganizationId;
	}

	public String getCorrelationKey()
	{
		return correlationKey;
	}
}
