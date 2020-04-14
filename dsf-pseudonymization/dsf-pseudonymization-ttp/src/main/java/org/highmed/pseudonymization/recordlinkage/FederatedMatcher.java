package org.highmed.pseudonymization.recordlinkage;

import java.util.List;
import java.util.Set;

public interface FederatedMatcher<P extends Person>
{
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
	Set<MatchedPerson<P>> matchPersons(List<P> personList, @SuppressWarnings("unchecked") List<P>... personLists);

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
	Set<MatchedPerson<P>> matchPersons(List<List<P>> personLists);
}