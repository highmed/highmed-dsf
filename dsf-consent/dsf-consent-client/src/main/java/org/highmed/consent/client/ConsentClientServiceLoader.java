package org.highmed.consent.client;

import java.util.Optional;
import java.util.ServiceLoader;

public class ConsentClientServiceLoader
{
	public Optional<ConsentClientFactory> getConsentClientFactory(String factoryClassName)
	{
		ServiceLoader<ConsentClientFactory> factories = ServiceLoader.load(ConsentClientFactory.class);

		return factories.stream().map(ServiceLoader.Provider::get)
				.filter(f -> f.getClass().getName().equals(factoryClassName)).findFirst();
	}
}
