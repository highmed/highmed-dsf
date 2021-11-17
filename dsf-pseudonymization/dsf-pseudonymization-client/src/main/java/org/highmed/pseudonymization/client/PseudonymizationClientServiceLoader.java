package org.highmed.pseudonymization.client;

import java.util.Optional;
import java.util.ServiceLoader;

public class PseudonymizationClientServiceLoader
{
	public Optional<PseudonymizationClientFactory> getPseudonymizationClientFactory(String factoryClassName)
	{
		ServiceLoader<PseudonymizationClientFactory> factories = ServiceLoader
				.load(PseudonymizationClientFactory.class);

		return factories.stream().map(ServiceLoader.Provider::get)
				.filter(f -> f.getClass().getName().equals(factoryClassName)).findFirst();
	}
}
