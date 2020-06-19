package org.highmed.mpi.client;

import java.util.Optional;
import java.util.ServiceLoader;

public class MasterPatientIndexClientServiceLoader
{
	public Optional<MasterPatientIndexClientFactory> getMasterPatientIndexClientFactory(String factoryClassName)
	{
		ServiceLoader<MasterPatientIndexClientFactory> factories = ServiceLoader
				.load(MasterPatientIndexClientFactory.class);

		return factories.stream().map(ServiceLoader.Provider::get)
				.filter(f -> f.getClass().getName().equals(factoryClassName)).findFirst();
	}
}
