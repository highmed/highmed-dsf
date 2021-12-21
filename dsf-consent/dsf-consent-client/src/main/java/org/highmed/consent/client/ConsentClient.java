package org.highmed.consent.client;

import org.highmed.openehr.model.structure.ResultSet;

/**
 * Client interface to check consent based on a {@link ResultSet}
 */
public interface ConsentClient
{
	String EHRID_COLUMN_DEFAULT_NAME = "EHRID";
	String EHRID_COLUMN_DEFAULT_PATH = "/ehr_status/subject/external_ref/id/value";

	/**
	 * Filters a given {@link ResultSet} based on an openEHR-EHR-ID and positive consent, rows without consent will be
	 * removed.
	 *
	 * @param resultSet
	 *            must have a column containing the openEHR-EHR-ID, not <code>null</code>
	 * @return a {@link ResultSet} based on an openEHR-EHR-ID and rows filtered by positive consent
	 * @throws IllegalArgumentException
	 *             if resultSet does not contain a openEHR-EHR-ID column
	 */
	ResultSet removeRowsWithoutConsent(ResultSet resultSet);
}
