package org.highmed.pseudonymization.recordlinkage;

import java.util.List;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "@type")
public interface MatchedPerson<P extends Person>
{
	/**
	 * @return list of matched persons, sorted in the order in which they were added
	 * @see #addMatch(Person)
	 */
	List<P> getMatches();

	default P getFirstMatch() throws NoSuchElementException
	{
		if (getMatches().isEmpty())
			throw new NoSuchElementException();
		else
			return getMatches().get(0);
	}

	default P getLastMatch() throws NoSuchElementException
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
	void addMatch(P person);
}
