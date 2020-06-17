package org.highmed.mpi.client;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterPatientIndexClientServiceLoader
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexClientServiceLoader.class);

	public MasterPatientIndexClientFactory getMasterPatientIndexClientFactory(String factoryClassName)
	{
		ServiceLoader<MasterPatientIndexClientFactory> factories = ServiceLoader.load(MasterPatientIndexClientFactory.class);
		
		factories.forEach(f -> System.out.println("factory loaded: " + f.getClass().getName()));

		for(MasterPatientIndexClientFactory factory : factories)
		{
			if (factory.getClass().getName().equals(factoryClassName))
				return factory;
		}

		logger.warn("Could not load master patient index client factory with classname='{}'", factoryClassName);
		return null;
	}
}
