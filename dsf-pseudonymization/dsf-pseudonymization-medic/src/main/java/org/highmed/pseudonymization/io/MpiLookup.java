package org.highmed.pseudonymization.io;

import org.highmed.pseudonymization.base.Idat;

/**
 * Utility class for fetching a subject's IDAT from
 * a Master Patient Index. To be implemented
 * individually by MeDiCs.
 */
public interface MpiLookup
{

	/**
	 * Perform an MPI Lookup for a given openEHR-EHR-ID,
	 * fetching the subject's MPI ID as well as their
	 * IDAT (First name, last name, sex, birthday, zipCode,
	 * city, country, insurance no.)
	 *
	 * @param ehrID A subject's ehrID String as returned in OpenEHR
	 *              result sets
	 * @return A filled {@link IdatContainer}
	 */
	public Idat fetchIdat(String ehrID);
}
