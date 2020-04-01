package org.highmed.pseudonymization.recordlinkage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SingleOrganizationMatcher extends AbstractMatcher
{
	/**
	 * See {@link AbstractMatcher#AbstractEpiLinkMatcher()}
	 */
	public SingleOrganizationMatcher()
	{
	}

	/**
	 * See {@link AbstractMatcher#AbstractEpiLinkMatcher(MatchCalculator)}
	 * 
	 * @param matchCalculator
	 *            not <code>null</code>
	 */
	public SingleOrganizationMatcher(MatchCalculator matchCalculator)
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
	public SingleOrganizationMatcher(MatchCalculator matchCalculator, double positiveMatchThreshold)
	{
		super(matchCalculator, positiveMatchThreshold);
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
	public Set<MatchedPerson> matchPersons(Person person, Person... persons)
	{
		Objects.requireNonNull(person, "person");
		Objects.requireNonNull(persons, "persons");

		if (persons.length == 0)
			return Collections.singleton(person.toMatchedPerson());
		else
		{
			List<Person> list = new ArrayList<>(1 + persons.length);
			list.add(person);
			list.addAll(Arrays.asList(persons));

			return matchPersons(list);
		}
	}

	/**
	 * Matches {@link Person}s from a single organizations - local matching.
	 * 
	 * @param persons
	 * @return matched persons, converted person from param {@code persons} if param {@code persons} has only one entry,
	 *         empty list if param {@code persons} has no entries
	 */
	public Set<MatchedPerson> matchPersons(List<Person> persons)
	{
		Objects.requireNonNull(persons, "persons");

		if (persons.isEmpty())
			return Collections.emptySet();
		else if (persons.size() == 1)
			return Collections.singleton(persons.get(0).toMatchedPerson());
		else
		{
			Set<MatchedPerson> matchedPersons = new HashSet<>();
			matchedPersons.add(persons.get(0).toMatchedPerson());

			persons.stream().skip(1).forEach(person ->
			{
				MatchedPerson matchedPerson = matchPerson(person, matchedPersons);
				matchedPersons.add(matchedPerson);
			});

			return Collections.unmodifiableSet(matchedPersons);
		}
	}
}
