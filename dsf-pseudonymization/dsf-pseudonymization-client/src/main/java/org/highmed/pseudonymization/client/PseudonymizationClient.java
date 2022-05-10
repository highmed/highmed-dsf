package org.highmed.pseudonymization.client;

import org.highmed.openehr.model.structure.ResultSet;

/**
 * Client interface to check consent based on a {@link ResultSet}
 */
public interface PseudonymizationClient
{
	/**
	 * Pseudonymizes row values of a given {@link ResultSet} whilst keeping column names and order
	 *
	 * @param resultSet
	 *            where row values should be pseudonymized, not <code>null</code>
	 * @return a {@link ResultSet} with pseudonymized row values, column names and order should be the same as in the
	 *         input {@link ResultSet}
	 */
	ResultSet pseudonymize(ResultSet resultSet);
}
