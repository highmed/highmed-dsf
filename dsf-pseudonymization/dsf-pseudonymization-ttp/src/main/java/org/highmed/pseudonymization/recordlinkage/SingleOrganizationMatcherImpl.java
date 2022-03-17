package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Single organization record linkage matcher implementation to match {@link Person}s from a single organization.
 *
 * @param <P>
 *            the type of the persons matched by this single organization matcher
 */
public class SingleOrganizationMatcherImpl<P extends Person> extends AbstractMatcher<P>
		implements SingleOrganizationMatcher<P>
{
	/**
	 * See {@link AbstractMatcher#AbstractMatcher(MatchedPersonFactory)}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 */
	public SingleOrganizationMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory)
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
	public SingleOrganizationMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator)
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
	public SingleOrganizationMatcherImpl(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator,
			double positiveMatchThreshold)
	{
		super(matchedPersonFactory, matchCalculator, positiveMatchThreshold);
	}

	/**
	 * Matches {@link Person}s from a single organizations - local matching.
	 *
	 * @param person
	 *            not <code>null</code>
	 * @param persons
	 *            not <code>null</code>, may be of length 0
	 * @return matched persons, converted person from param {@code person} if param {@code persons} is empty
	 * @see #matchPersons(List)
	 */
	@Override
	public Set<MatchedPerson<P>> matchPersons(P person, @SuppressWarnings("unchecked") P... persons)
	{
		Objects.requireNonNull(person, "person");
		Objects.requireNonNull(persons, "persons");

		if (persons.length == 0)
			return Collections.singleton(create(person));
		else
		{
			List<P> list = new ArrayList<>(1 + persons.length);
			list.add(person);
			list.addAll(Arrays.asList(persons));

			return matchPersons(list);
		}
	}

	/**
	 * Matches {@link Person}s from a single organizations - local matching.
	 *
	 * @param persons
	 *            not <code>null</code>
	 * @return matched persons, converted person from param {@code persons} if param {@code persons} has only one entry,
	 *         empty list if param {@code persons} has no entries
	 */
	@Override
	public Set<MatchedPerson<P>> matchPersons(List<P> persons)
	{
		Objects.requireNonNull(persons, "persons");

		if (persons.isEmpty())
			return Collections.emptySet();
		else if (persons.size() == 1)
			return Collections.singleton(create(persons.get(0)));
		else
		{
			Set<MatchedPerson<P>> matchedPersons = new HashSet<>();
			matchedPersons.add(create(persons.get(0)));

			persons.stream().skip(1).forEach(person ->
			{
				MatchedPerson<P> matchedPerson = matchPerson(person, matchedPersons);
				matchedPersons.add(matchedPerson);
			});

			return Collections.unmodifiableSet(matchedPersons);
		}
	}
}
