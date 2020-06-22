package org.highmed.mpi.client;

import java.util.function.Function;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient createClient(Function<String, String> propertyResolver);
}
