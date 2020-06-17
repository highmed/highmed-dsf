package org.highmed.mpi.client;

import org.springframework.core.env.Environment;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient getWebserviceClient(Environment environment);
}
