package org.highmed.dsf.bpe.variables;

import java.io.Serializable;

public class FinalSimpleFeasibilityResult implements Serializable
{
	private String cohortId;
	private long participatingMedics;
	private long cohortSize;

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
