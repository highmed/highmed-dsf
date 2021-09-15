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
	 * Filters a given {@link ResultSet} based on an openEHR-EHR-ID and positive consent
	 *
	 * @param resultSet
	 *            must have a row containing the openEHR-EHR-ID, not <code>null</code>
	 * @return a {@link ResultSet} based on an openEHR-EHR-ID and positive consent
	 */
	ResultSet checkConsent(ResultSet resultSet);
}
