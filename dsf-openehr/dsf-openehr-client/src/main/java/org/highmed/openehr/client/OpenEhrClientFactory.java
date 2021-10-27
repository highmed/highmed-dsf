package org.highmed.openehr.client;

import java.util.function.BiFunction;

public interface OpenEhrClientFactory
{
	/**
	 * @param propertyResolver
	 *            arg1 = property name, arg2 = default value, return = value or default value if property name does not
	 *            exist or is null, not <code>null</code>
	 * @return initialized {@link OpenEhrClient}, not <code>null</code>
	 */
	OpenEhrClient createClient(BiFunction<String, String, String> propertyResolver);
}
