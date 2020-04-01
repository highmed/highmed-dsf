package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestMatchedPerson implements MatchedPerson
{
	private final List<Person> matches = new ArrayList<>();

	public TestMatchedPerson(Person person)
	{
		if (person != null)
			addMatch(person);
	}

	@Override
	public List<Person> getMatches()
	{
		return Collections.unmodifiableList(matches);
	}

	@Override
	public void addMatch(Person person)
	{
		if (person != null)
			matches.add(person);
	}

	@Override
	public String toString()
	{
		return matches.stream().map(Person::toString).collect(Collectors.joining(", ", "[", "]"));
	}
}
