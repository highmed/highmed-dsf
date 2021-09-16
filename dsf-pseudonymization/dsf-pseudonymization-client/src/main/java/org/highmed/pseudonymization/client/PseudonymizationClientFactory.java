package org.highmed.pseudonymization.client;

import java.util.function.BiFunction;

public interface PseudonymizationClientFactory
{
	PseudonymizationClient createClient(BiFunction<String, String, String> propertyResolver);
}
