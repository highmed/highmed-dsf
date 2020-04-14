package org.highmed.pseudonymization.recordlinkage;

import java.util.List;
import java.util.Set;

public interface SingleOrganizationMatcher<P extends Person>
{
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
	Set<MatchedPerson<P>> matchPersons(P person, @SuppressWarnings("unchecked") P... persons);

	/**
	 * Matches {@link Person}s from a single organizations - local matching.
	 * 
	 * @param persons
	 * @return matched persons, converted person from param {@code persons} if param {@code persons} has only one entry,
	 *         empty list if param {@code persons} has no entries
	 */
	Set<MatchedPerson<P>> matchPersons(List<P> persons);
}