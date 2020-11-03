package org.highmed.openehr.client;

import java.util.function.Function;

public interface OpenEhrClientFactory
{
	OpenEhrClient createClient(Function<String, String> propertyResolver);
}
