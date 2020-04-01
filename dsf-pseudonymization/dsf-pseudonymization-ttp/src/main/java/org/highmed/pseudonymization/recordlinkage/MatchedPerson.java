package org.highmed.pseudonymization.recordlinkage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MatchedPerson
{
	/**
	 * @return list of matched persons, sorted in the order in which they were added
	 * @see #addMatch(Person)
	 */
	List<Person> getMatches();

	default Person getFirstMatch() throws NoSuchElementException
	{
		if (getMatches().isEmpty())
			throw new NoSuchElementException();
		else
			return getMatches().get(0);
	}

	default Person getLastMatch() throws NoSuchElementException
	{
		if (getMatches().isEmpty())
			throw new NoSuchElementException();
		else
			return getMatches().get(getMatches().size() - 1);
	}

	/**
	 * @param person
	 *            not <code>null</code>
	 */
	void addMatch(Person person);

	static Function<MatchedPerson, MatchedPerson> add(Person person)
	{
		return matchedPerson ->
		{
			matchedPerson.addMatch(person);
			return matchedPerson;
		};
	}

	static Supplier<MatchedPerson> from(Person person)
	{
		return () -> person.toMatchedPerson();
	}
}
