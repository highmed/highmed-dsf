package org.highmed.dsf.fhir.variables;

import java.io.Serializable;

// TODO: check if Serializable can be replaced by JSON serialization
public class FeasibilityQueryResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String organizationIdentifier;
	private final String cohortId;
	private final int cohortSize;

	public FeasibilityQueryResult(String organizationIdentifier, String groupId, int groupSize)
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
