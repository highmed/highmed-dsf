package org.highmed.openehr.client;

import java.util.Optional;
import java.util.ServiceLoader;

public class OpenEhrClientServiceLoader
{
	public Optional<OpenEhrClientFactory> getOpenEhrClientFactory(String factoryClassName)
	{
		ServiceLoader<OpenEhrClientFactory> factories = ServiceLoader.load(OpenEhrClientFactory.class);

		return factories.stream().map(ServiceLoader.Provider::get)
				.filter(f -> f.getClass().getName().equals(factoryClassName)).findFirst();
	}
}
