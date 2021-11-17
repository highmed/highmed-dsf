package org.highmed.mpi.client.stub;

import java.util.function.BiFunction;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;

public class MasterPatientIndexClientStubFactory implements MasterPatientIndexClientFactory
{
	@Override
	public MasterPatientIndexClient createClient(BiFunction<String, String, String> propertyResolver)
	{
		return new MasterPatientIndexClientStub();
	}
}
