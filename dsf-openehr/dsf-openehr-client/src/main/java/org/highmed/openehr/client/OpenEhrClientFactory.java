package org.highmed.openehr.client;

import java.util.function.BiFunction;

public interface OpenEhrClientFactory
{
	OpenEhrClient createClient(BiFunction<String, String, String> propertyResolver);
}
