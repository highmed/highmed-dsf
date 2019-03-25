package org.highmed.fhir.variables;

import org.hl7.fhir.r4.model.Organization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceTarget
{
	private final Organization targetOrganization;
	private final String correlationKey;

	@JsonCreator
	public MultiInstanceTarget(@JsonProperty("targetOrganization") Organization targetOrganization,
			@JsonProperty("correlationKey") String correlationKey)
	{
		this.targetOrganization = targetOrganization;
		this.correlationKey = correlationKey;
	}

	public Organization getTargetOrganization()
	{
		return targetOrganization;
	}

	public String getCorrelationKey()
	{
		return correlationKey;
	}
}
