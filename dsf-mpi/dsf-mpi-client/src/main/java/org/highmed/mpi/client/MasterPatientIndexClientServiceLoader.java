package org.highmed.mpi.client;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterPatientIndexClientServiceLoader
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexClientServiceLoader.class);

	public MasterPatientIndexClientFactory getMasterPatientIndexClientFactory(String factoryClassName)
	{
		ServiceLoader<MasterPatientIndexClientFactory> factories = ServiceLoader
				.load(MasterPatientIndexClientFactory.class);

		factories.forEach(f -> logger.debug("Found master patient index client factory with classname='{}'",
				f.getClass().getName()));

		for (MasterPatientIndexClientFactory factory : factories)
		{
			if (factory.getClass().getName().equals(factoryClassName))
				logger.debug("Using master patient index client factory with classname='{}'", factoryClassName);
			return factory;
		}

		logger.warn("Did not find any master patient index client factory with classname='{}'", factoryClassName);
		return null;
	}
}
