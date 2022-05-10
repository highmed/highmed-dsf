package org.highmed.dsf.fhir.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

	/**
	 * @param target
	 * @return true if the given target was part of the entries list
	 * @deprecated remove based on {@link Object#equals(Object)} may not work due to
	 * @see #removeByEndpointIdentifierValue(Target)
	 */
	@Deprecated
	public boolean removeTarget(Target target)
	{
		return entries.remove(target);
	}

	/**
	 * Removes targets base on the given {@link Target}s endpoint identifier value.
	 *
	 * @param target
	 * @return new {@link Targets} object
	 * @see Target#getEndpointIdentifierValue()
	 */
	public Targets removeByEndpointIdentifierValue(Target target)
	{
		if (target == null)
			return new Targets(entries);

		Targets newTargets = removeByEndpointIdentifierValue(target.getEndpointIdentifierValue());

		// if the old target factory method is used, the target object does not have an endpoint identifier set
		if (target.getEndpointIdentifierValue() == null)
			newTargets.removeTarget(target);

		return newTargets;
	}

	/**
	 * Removes targets base on the given endpoint identifier value.
	 *
	 * @param targetEndpointIdentifierValue
	 * @return new {@link Targets} object
	 */
	public Targets removeByEndpointIdentifierValue(String targetEndpointIdentifierValue)
	{
		if (targetEndpointIdentifierValue == null)
			return new Targets(entries);

		return new Targets(
				entries.stream().filter(t -> !targetEndpointIdentifierValue.equals(t.getEndpointIdentifierValue()))
						.collect(Collectors.toList()));
	}

	/**
	 * Removes targets base on the given endpoint identifier values.
	 *
	 * @param targetEndpointIdentifierValues
	 * @return new {@link Targets} object
	 */
	public Targets removeAllByEndpointIdentifierValue(Collection<String> targetEndpointIdentifierValues)
	{
		if (targetEndpointIdentifierValues == null || targetEndpointIdentifierValues.isEmpty())
			return new Targets(entries);

		return new Targets(
				entries.stream().filter(t -> !targetEndpointIdentifierValues.contains(t.getEndpointIdentifierValue()))
						.collect(Collectors.toList()));
	}

	@JsonIgnore
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
}
