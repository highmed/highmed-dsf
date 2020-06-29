package org.highmed.pseudonymization.recordlinkage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestMedicId implements MedicId
{
	private final String organization;
	private final String value;

	@JsonCreator
	public TestMedicId(@JsonProperty("organization") String organization, @JsonProperty("value") String value)
	{
		this.organization = organization;
		this.value = value;
	}

	@Override
	public String getOrganization()
	{
		return organization;
	}

	@Override
	public String getValue()
	{
		return value;
	}
}
