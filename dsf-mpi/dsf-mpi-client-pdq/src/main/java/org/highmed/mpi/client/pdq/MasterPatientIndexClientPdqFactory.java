package org.highmed.mpi.client.pdq;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.security.CustomSocketFactory;
import org.highmed.mpi.client.security.CustomSslFactory;
import org.springframework.core.env.Environment;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.util.SocketFactory;

public class MasterPatientIndexClientPdqFactory implements MasterPatientIndexClientFactory
{
	@Override
	public MasterPatientIndexClient build(Environment environment)
	{
		// TODO: read values from environment
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

		return new MasterPatientIndexClientPdq(host, port, senderApplication,
				senderFacility, receiverApplication, receiverFacility, pidAssigningAuthorityNamespaceId,
				pidAssigningAuthorityUniversalId, messageHelper, context, socketFactory);
	}
}
