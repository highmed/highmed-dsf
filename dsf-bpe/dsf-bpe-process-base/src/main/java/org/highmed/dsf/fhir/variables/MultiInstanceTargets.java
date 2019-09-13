package org.highmed.dsf.fhir.variables;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceTargets
{
	private final List<MultiInstanceTarget> targets;

	@JsonCreator
	public MultiInstanceTargets(@JsonProperty("targets") List<MultiInstanceTarget> targets)
	{
		this.targets = targets;
	}

	public List<MultiInstanceTarget> getTargets()
	{
		return targets;
	}
}
