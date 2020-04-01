package org.highmed.pseudonymization.psn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MedicId
{
	private final String organization;
	private final String value;

	@JsonCreator
	public MedicId(@JsonProperty("organization") String organization, @JsonProperty("value") String value)
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