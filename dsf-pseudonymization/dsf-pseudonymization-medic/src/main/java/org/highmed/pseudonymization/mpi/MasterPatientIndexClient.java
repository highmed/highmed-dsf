package org.highmed.pseudonymization.mpi;

/**
 * Client interface for fetching a subject's IDAT from a Master Patient Index.
 */
public interface MasterPatientIndexClient
{
	/**
	 * Perform an MPI Lookup for a given openEHR-EHR-ID, fetching the subject's MPI ID as well as their IDAT (First
	 * name, last name, sex, birthday, zipCode, city, country, insurance no.)
	 *
	 * @param ehrID
	 *            A subject's ehrID String as returned in OpenEHR result sets
	 * @return A filled {@link Idat}
	 */
	Idat fetchIdat(String ehrID);
}
