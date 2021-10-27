package org.highmed.mpi.client;

import java.util.function.BiFunction;

public interface MasterPatientIndexClientFactory
{
	/**
	 * @param propertyResolver
	 *            arg1 = property name, arg2 = default value, return = value or default value if property name does not
	 *            exist or is null, not <code>null</code>
	 * @return initialized {@link MasterPatientIndexClient}, not <code>null</code>
	 */
	MasterPatientIndexClient createClient(BiFunction<String, String, String> propertyResolver);
}
