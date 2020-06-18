package org.highmed.mpi.client;

import java.util.Properties;

public class MpiRunner
{
	public static void main(String[] args)
	{
		Properties properties = new Properties();

		properties.put("org.highmed.dsf.bpe.mpi.pdq.webservice.host", "161.42.236.185");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.webservice.port", "3750");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.path", "test-client_certificate.p12");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.password", "changeit");

		properties.put("org.highmed.dsf.bpe.mpi.pdq.sender.application", "MBE01");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.sender.facility", "MeDIC");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.receiver.application", "ICW-MPI");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.receiver.facility", "ICW");

		properties.put("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.namespaceId", "SAP-ISH");
		properties.put("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.universalId", "1.2.276.0.76.3.1.78.1.0.10.1.101.1");

		MasterPatientIndexClientFactory masterPatientIndexClientFactory = new MasterPatientIndexClientServiceLoader()
				.getMasterPatientIndexClientFactory("org.highmed.mpi.client.pdq.MasterPatientIndexClientPdqFactory");
		MasterPatientIndexClient mpiClient = masterPatientIndexClientFactory.getWebserviceClient(properties);

		Idat idat = mpiClient.fetchIdat("0002036518");
		System.out.println(idat.toString());
	}
}
