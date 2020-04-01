package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FederatedMatcher extends AbstractMatcher
{
	/**
	 * See {@link AbstractMatcher#AbstractEpiLinkMatcher()}
	 */
	public FederatedMatcher()
	{
	}

	/**
	 * See {@link AbstractMatcher#AbstractEpiLinkMatcher(MatchCalculator)}
	 * 
	 * @param matchCalculator
	 *            not <code>null</code>
	 */
	public FederatedMatcher(MatchCalculator matchCalculator)
	{
		super(matchCalculator);
	}

	/**
	 * See {@link AbstractMatcher#AbstractEpiLinkMatcher(MatchCalculator, double)}
	 * 
	 * @param matchCalculator
	 *            not <code>null</code>
	 * @param positiveMatchThreshold
	 */
	public FederatedMatcher(MatchCalculator matchCalculator, double positiveMatchThreshold)
	{
		super(matchCalculator, positiveMatchThreshold);
	}

	/**
	 * Matches {@link Person}s from multiple organizations, expects the {@link Person}s to be unique within
	 * organizations - distributed matching.
	 * 
	 * @param personList
	 *            not <code>null</code>
	 * @param personLists
	 *            not <code>null</code>, may be of length 0
	 * @return matched persons, converted persons from param {@code personList} if param {@code personLists} is empty
	 * @see #matchPersons(List)
	 */
	public Set<MatchedPerson> matchPersons(List<Person> personList,
			@SuppressWarnings("unchecked") List<Person>... personLists)
	{
		Objects.requireNonNull(personList, "personList");
		Objects.requireNonNull(personLists, "personLists");

		if (personLists.length == 0)
			return personList.stream().map(Person::toMatchedPerson).collect(Collectors.toSet());
		else
		{
			List<List<Person>> lists = new ArrayList<>(1 + personLists.length);
			lists.add(personList);
			lists.addAll(Arrays.asList(personLists));

			return matchPersons(lists);
		}
	}

	/**
	 * Matches {@link Person}s from multiple organizations, expects the {@link Person}s to be unique within
	 * organizations - distributed matching.
	 * 
	 * @param personLists
	 *            not <code>null</code>
	 * @return matched persons, converted persons from param {@code personLists} if param {@code personLists} has only
	 *         one entry (aka one organization), empty list if param {@code personLists} has no entries
	 * @see #matchPersons(List, List...)
	 */
	public Set<MatchedPerson> matchPersons(List<List<Person>> personLists)
	{
		Objects.requireNonNull(personLists, "personLists");

		if (personLists.isEmpty())
			return Collections.emptySet();
		else if (personLists.size() == 1)
			return personLists.get(0).stream().map(Person::toMatchedPerson).collect(Collectors.toSet());
		else
		{
			List<Person> largestList = findLargestList(personLists);
			List<List<Person>> remainingLists = exceptLargest(personLists, largestList);
			Set<MatchedPerson> matchedPersons = toMatchedPersons(largestList);

			for (List<Person> personList : remainingLists)
				matchedPersons = matchPersonList(personList, matchedPersons);

			return Collections.unmodifiableSet(matchedPersons);
		}
	}

	private List<Person> findLargestList(List<List<Person>> personLists)
	{
		return personLists.stream().max(Comparator.comparing(List::size)).orElseThrow();
	}

	private List<List<Person>> exceptLargest(List<List<Person>> personLists, List<Person> largestList)
	{
		List<List<Person>> remainingLists = new ArrayList<List<Person>>(personLists);
		remainingLists.remove(largestList);
		return remainingLists;
	}

	private Set<MatchedPerson> toMatchedPersons(List<Person> largestList)
	{
		return largestList.parallelStream().map(Person::toMatchedPerson).collect(Collectors.toSet());
	}

	private Set<MatchedPerson> matchPersonList(List<Person> personList, Set<MatchedPerson> matchedPersons)
	{
		Set<MatchedPerson> newMatches = personList.parallelStream().map(matchPerson(matchedPersons))
				.collect(Collectors.toCollection(HashSet::new));
		newMatches.addAll(matchedPersons);

		return newMatches;
	}
}
