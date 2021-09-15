package org.highmed.consent.client;

import java.util.function.BiFunction;

public interface ConsentClientFactory
{
	ConsentClient createClient(BiFunction<String, String, String> propertyResolver);
}
