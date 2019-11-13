package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceTargets
{
	private final List<MultiInstanceTarget> targets = new ArrayList<>();

	@JsonCreator
	public MultiInstanceTargets(@JsonProperty("targets") Collection<? extends MultiInstanceTarget> targets)
	{
		if (targets != null)
			this.targets.addAll(targets);
	}

	public List<MultiInstanceTarget> getTargets()
	{
		return Collections.unmodifiableList(targets);
	}

	public void removeTarget(MultiInstanceTarget target)
	{
		targets.remove(target);
	}
}
