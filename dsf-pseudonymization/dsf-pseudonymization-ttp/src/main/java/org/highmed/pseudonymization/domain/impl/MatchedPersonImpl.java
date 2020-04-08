package org.highmed.pseudonymization.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.pseudonymization.domain.MatchedPersonWithMdat;
import org.highmed.pseudonymization.domain.MdatContainer;
import org.highmed.pseudonymization.domain.PersonWithMdat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchedPersonImpl implements MatchedPersonWithMdat
{
	private final List<PersonWithMdat> matches = new ArrayList<>();
	private final List<MdatContainer> mdatContainers = new ArrayList<>();

	public MatchedPersonImpl(PersonWithMdat person, MdatContainer mdatContainer)
	{
		if (person != null)
			matches.add(person);

		if (mdatContainer != null)
			mdatContainers.add(mdatContainer);
	}

	@JsonCreator
	public MatchedPersonImpl(@JsonProperty("matches") Collection<? extends PersonWithMdat> matches,
			@JsonProperty("mdatContainers") Collection<? extends MdatContainer> mdatContainers)
	{
		if (matches != null)
			this.matches.addAll(matches);

		if (mdatContainers != null)
			this.mdatContainers.addAll(mdatContainers);
	}

	@Override
	public List<PersonWithMdat> getMatches()
	{
		return Collections.unmodifiableList(matches);
	}

	@Override
	public void addMatch(PersonWithMdat person)
	{
		if (person != null)
			matches.add(person);
	}

	@Override
	public List<MdatContainer> getMdatContainers()
	{
		return Collections.unmodifiableList(mdatContainers);
	}
}
