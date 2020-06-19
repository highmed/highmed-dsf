package org.highmed.mpi.client;

import java.util.function.Function;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient getWebserviceClient(Function<String, String> propertyResolver);
}
