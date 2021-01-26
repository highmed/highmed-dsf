package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Targets
{
	private final List<Target> entries = new ArrayList<>();

	@JsonCreator
	public Targets(@JsonProperty("entries") Collection<? extends Target> targets)
	{
		if (targets != null)
			this.entries.addAll(targets);
	}

	public List<Target> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	public void removeTarget(Target target)
	{
		entries.remove(target);
	}
}
