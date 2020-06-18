package org.highmed.mpi.client;

import java.util.Properties;

import org.springframework.core.env.Environment;

public interface MasterPatientIndexClientFactory
{
	MasterPatientIndexClient getWebserviceClient(Environment environment);

	MasterPatientIndexClient getWebserviceClient(Properties properties);
}
