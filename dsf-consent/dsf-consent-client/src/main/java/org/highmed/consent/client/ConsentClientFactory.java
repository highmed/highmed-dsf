package org.highmed.consent.client;

import java.util.function.BiFunction;

public interface ConsentClientFactory
{
	/**
	 * @param propertyResolver
	 *            arg1 = property name, arg2 = default value, return = value or default value if property name does not
	 *            exist or is null, not <code>null</code>
	 * @return initialized {@link ConsentClient}, not <code>null</code>
	 */
	ConsentClient createClient(BiFunction<String, String, String> propertyResolver);
}
