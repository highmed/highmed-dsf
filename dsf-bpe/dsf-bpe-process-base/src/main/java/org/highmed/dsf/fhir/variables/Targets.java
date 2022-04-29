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

	@JsonProperty("entries")
	public List<Target> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	@Deprecated
	public boolean removeTarget(Target target)
	{
		return entries.remove(target);
	}

	public Targets remove(Target target)
	{
		Targets newTargets = new Targets(entries);
		newTargets.entries.remove(target);
		return newTargets;
	}

	public Targets removeAll(Collection<?> c)
	{
		Targets newTargets = new Targets(entries);
		newTargets.entries.removeAll(c);
		return newTargets;
	}

	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
}
