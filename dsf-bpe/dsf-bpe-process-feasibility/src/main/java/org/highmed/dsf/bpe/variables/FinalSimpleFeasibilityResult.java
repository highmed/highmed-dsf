package org.highmed.dsf.bpe.variables;

import java.io.Serializable;

// TODO: check if Serializable can be replaced by JSON serialization
public class FinalSimpleFeasibilityResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String cohortId;
	private final long participatingMedics;
	private final long cohortSize;

	public FinalSimpleFeasibilityResult(String cohortId, long participatingMedics, long cohortSize)
	{
		this.cohortId = cohortId;
		this.participatingMedics = participatingMedics;
		this.cohortSize = cohortSize;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public long getParticipatingMedics()
	{
		return participatingMedics;
	}

	public long getCohortSize()
	{
		return cohortSize;
	}
}

