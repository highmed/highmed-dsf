package org.highmed.mpi.client;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;

public class MpiRunner
{
	public static void main(String[] args) throws IOException
	{
		Properties properties = new Properties();
		properties.load(ClassLoader.getSystemResourceAsStream("config.properties"));

		Optional<MasterPatientIndexClientFactory> masterPatientIndexClientFactory = new MasterPatientIndexClientServiceLoader()
				.getMasterPatientIndexClientFactory("org.highmed.mpi.client.pdq.MasterPatientIndexClientPdqFactory");

		MasterPatientIndexClient mpiClient = masterPatientIndexClientFactory
				.orElseThrow(() -> new NoSuchElementException("Master patient index client factory not found"))
				.getWebserviceClient(properties::getProperty);

		Idat idat = mpiClient.fetchIdat("0002036518");
		System.out.println(idat.toString());
	}
}
