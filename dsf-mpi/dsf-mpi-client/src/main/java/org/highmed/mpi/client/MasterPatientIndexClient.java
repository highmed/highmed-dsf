package org.highmed.mpi.client;

/**
 * Client interface for fetching a subject's IDAT from a Master Patient Index.
 */
public interface MasterPatientIndexClient
{
	/**
	 * Perform an MPI Lookup for a given openEHR-EHR-ID, fetching the subject's MPI ID as well as their IDAT (First
	 * name, last name, sex, birthday, zipCode, city, country, insurance no.)
	 *
	 * @param ehrId
	 *            A subject's ehrID String as returned in OpenEHR result sets
	 * @return A filled {@link Idat}
	 * @throws IdatNotFoundException
	 *             if not IDAT could be found for the given ehrId
	 */
	Idat fetchIdat(String ehrId) throws IdatNotFoundException;
}
