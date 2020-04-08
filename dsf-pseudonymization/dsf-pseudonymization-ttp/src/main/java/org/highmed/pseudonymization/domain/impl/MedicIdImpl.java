package org.highmed.pseudonymization.domain.impl;

import org.highmed.pseudonymization.recordlinkage.MedicId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MedicIdImpl implements MedicId
{
	private final String organization;
	private final String value;

	@JsonCreator
	public MedicIdImpl(@JsonProperty("organization") String organization, @JsonProperty("value") String value)
	{
		this.organization = organization;
		this.value = value;
	}

	public String getOrganization()
	{
		return organization;
	}

	public String getValue()
	{
		return value;
	}
}