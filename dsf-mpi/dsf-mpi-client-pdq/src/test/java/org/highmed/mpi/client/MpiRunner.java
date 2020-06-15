package org.highmed.mpi.client;

import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.pdq.MasterPatientIndexClientPdq;
import org.highmed.mpi.client.security.CustomSocketFactory;
import org.highmed.mpi.client.security.CustomSslFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.util.SocketFactory;

public class MpiRunner
{
	public static void main(String[] args)
	{
		String host = "161.42.236.185";
		int port = 3750;

		String senderApplication = "MBE01";
		String senderFacility = "MeDIC";
		String receiverApplication = "ICW-MPI";
		String receiverFacility = "ICW";

		String pidAssigningAuthorityNamespaceId = "SAP-ISH";
		String pidAssigningAuthorityUniversalId = "1.2.276.0.76.3.1.78.1.0.10.1.101.1";

		MessageHelper messageHelper = new MessageHelper();
		HapiContext context = new DefaultHapiContext();

		String keystorePath = "test-client_certificate.p12";
		String keystorePassword = "changeit";
		CustomSslFactory sslFactory = new CustomSslFactory();
		SocketFactory socketFactory = new CustomSocketFactory(keystorePath, keystorePassword, sslFactory);

		MasterPatientIndexClientPdq mpiClient = new MasterPatientIndexClientPdq(host, port, senderApplication,
				senderFacility, receiverApplication, receiverFacility, pidAssigningAuthorityNamespaceId,
				pidAssigningAuthorityUniversalId, messageHelper, context, socketFactory);

		Idat idat = mpiClient.fetchIdat("0002036518");
		System.out.println(idat.toString());
	}
}
