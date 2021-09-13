package org.highmed.mpi.client;

import java.util.function.BiFunction;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient createClient(BiFunction<String, String, String> propertyResolver);
}
