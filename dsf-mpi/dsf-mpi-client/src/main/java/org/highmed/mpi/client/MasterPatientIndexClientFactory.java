package org.highmed.mpi.client;

import java.util.function.Function;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient createWebserviceClient(Function<String, String> propertyResolver);
}
