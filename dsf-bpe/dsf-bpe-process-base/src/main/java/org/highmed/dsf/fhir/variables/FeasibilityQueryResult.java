package org.highmed.dsf.fhir.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeasibilityQueryResult
{
	private final String organizationIdentifier;
	private final String cohortId;
	private final int cohortSize;

	@JsonCreator
	public FeasibilityQueryResult(
			@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("groupId") String groupId,
			@JsonProperty("groupSize") int groupSize)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.cohortId = groupId;
		this.cohortSize = groupSize;
	}

	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public int getCohortSize()
	{
		return cohortSize;
	}
}
