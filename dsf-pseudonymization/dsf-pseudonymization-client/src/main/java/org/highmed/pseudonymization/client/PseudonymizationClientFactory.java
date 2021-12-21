package org.highmed.pseudonymization.client;

import java.util.function.BiFunction;

public interface PseudonymizationClientFactory
{
	/**
	 * @param propertyResolver
	 *            arg1 = property name, arg2 = default value, return = value or default value if property name does not
	 *            exist or is null, not <code>null</code>
	 * @return initialized {@link PseudonymizationClient}, not <code>null</code>
	 */
	PseudonymizationClient createClient(BiFunction<String, String, String> propertyResolver);
}
