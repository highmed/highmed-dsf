package org.highmed.pseudonymization.recordlinkage;

import java.util.List;
import java.util.Set;

/**
 * Federated record linkage matcher to match {@link Person}s from multiple organizations. The matcher assumes that the
 * supplied lists of persons are unique per organization.
 *
 * @param <P>
 *            the type of the persons matched by this federated matcher
 */
public interface FederatedMatcher<P extends Person>
{
	/**
	 * Matches {@link Person}s from multiple organizations, expects the {@link Person}s to be unique within
	 * organizations - distributed matching.
	 *
	 * @param personLists
	 *            not <code>null</code>
	 * @return matched persons, converted persons from param {@code personLists} if param {@code personLists} has only
	 *         one entry (aka one organization), empty list if param {@code personLists} has no entries
	 */
	Set<MatchedPerson<P>> matchPersons(List<List<P>> personLists);
}