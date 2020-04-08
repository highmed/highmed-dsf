package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestMatchedPerson implements MatchedPerson<TestPerson>
{
	private final List<TestPerson> matches = new ArrayList<>();

	public TestMatchedPerson(TestPerson person)
	{
		if (person != null)
			addMatch(person);
	}

	public TestMatchedPerson(TestPerson... persons)
	{
		matches.addAll(Arrays.asList(persons));
	}

	@Override
	public List<TestPerson> getMatches()
	{
		return Collections.unmodifiableList(matches);
	}

	@Override
	public void addMatch(TestPerson person)
	{
		if (person != null)
			matches.add(person);
	}

	@Override
	public String toString()
	{
		return matches.stream().map(TestPerson::toString).collect(Collectors.joining(", ", "[", "]"));
	}
}
