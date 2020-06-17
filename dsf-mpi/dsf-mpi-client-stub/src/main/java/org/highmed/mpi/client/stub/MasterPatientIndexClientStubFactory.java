package org.highmed.mpi.client.stub;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.springframework.core.env.Environment;

public class MasterPatientIndexClientStubFactory implements MasterPatientIndexClientFactory
{
	@Override
	public MasterPatientIndexClient getWebserviceClient(Environment environment)
	{
		return new MasterPatientIndexClientStub();
	}
}
