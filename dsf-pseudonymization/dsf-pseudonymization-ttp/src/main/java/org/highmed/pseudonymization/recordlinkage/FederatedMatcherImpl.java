package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Federated record linkage matcher implementation to match {@link Person}s from multiple organizations. The matcher
 * assumes that the supplied lists of persons are unique per organization.
 *
 * @param <P>
 *            the type of the persons matched by this federated matcher
 */
public class FederatedMatcherImpl<P extends Person> extends AbstractMatcher<P> implements FederatedMatcher<P>
{
	/**
	 * See {@link AbstractMatcher#AbstractMatcher(MatchedPersonFactory)}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 */
	public FederatedMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory)
	{
		super(matchedPersonFactory);
	}

	/**
	 * See {@link AbstractMatcher#AbstractMatcher(MatchedPersonFactory, MatchCalculator)}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 * @param matchCalculator
	 *            not <code>null</code>
	 */
	public FederatedMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator)
	{
		super(matchedPersonFactory, matchCalculator);
	}

	/**
	 * See {@link AbstractMatcher#AbstractMatcher(MatchedPersonFactory, MatchCalculator, double)}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 * @param matchCalculator
	 *            not <code>null</code>
	 * @param positiveMatchThreshold
	 *            {@code >=0}
	 */
	public FederatedMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator,
			double positiveMatchThreshold)
	{
		super(matchedPersonFactory, matchCalculator, positiveMatchThreshold);
	}

	/**
	 * Matches {@link Person}s from multiple organizations, expects the {@link Person}s to be unique within
	 * organizations - distributed matching.
	 *
	 * @param personLists
	 *            not <code>null</code>
	 * @return matched persons, converted persons from param {@code personLists} if param {@code personLists} has only
	 *         one entry (aka one organization), empty list if param {@code personLists} has no entries
	 */
	@Override
	public Set<MatchedPerson<P>> matchPersons(List<List<P>> personLists)
	{
		Objects.requireNonNull(personLists, "personLists");

		if (personLists.isEmpty())
			return Collections.emptySet();
		else if (personLists.size() == 1)
			return personLists.get(0).stream().map(toMatchedPerson()).collect(Collectors.toSet());
		else
		{
			List<P> largestList = findLargestList(personLists);
			List<List<P>> remainingLists = exceptLargest(personLists, largestList);
			Set<MatchedPerson<P>> matchedPersons = toMatchedPersons(largestList);

			for (List<P> personList : remainingLists)
				matchedPersons = matchPersonList(personList, matchedPersons);

			return Collections.unmodifiableSet(matchedPersons);
		}
	}

	private List<P> findLargestList(List<List<P>> personLists)
	{
		return personLists.stream().max(Comparator.comparing(List::size)).orElseThrow();
	}

	private List<List<P>> exceptLargest(List<List<P>> personLists, List<P> largestList)
	{
		List<List<P>> remainingLists = new ArrayList<>(personLists);
		remainingLists.remove(largestList);
		return remainingLists;
	}

	private Set<MatchedPerson<P>> toMatchedPersons(List<P> largestList)
	{
		return largestList.parallelStream().map(toMatchedPerson()).collect(Collectors.toSet());
	}

	private Set<MatchedPerson<P>> matchPersonList(List<P> personList, Set<MatchedPerson<P>> matchedPersons)
	{
		Set<MatchedPerson<P>> newMatches = personList.stream().map(matchPerson(matchedPersons))
				.collect(Collectors.toCollection(HashSet::new));
		newMatches.addAll(matchedPersons);

		return newMatches;
	}
}
