package org.highmed.mpi.client;

import java.util.ServiceLoader;

public class MasterPatientIndexClientServiceLoader
{
	public MasterPatientIndexClient getWebServiceClient(String factoryClassName)
	{
		ServiceLoader<MasterPatientIndexClientFactory> factories = ServiceLoader.load(MasterPatientIndexClientFactory.class);
		
		factories.forEach(factory -> System.out.println("LOADED FACTORY " + factory.getClass().getName()));

		return null;
	}
}
