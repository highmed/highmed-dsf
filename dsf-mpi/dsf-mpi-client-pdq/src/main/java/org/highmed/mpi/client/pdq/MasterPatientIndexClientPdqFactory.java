package org.highmed.mpi.client.pdq;

import java.util.function.Function;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.security.CustomSocketFactory;
import org.highmed.mpi.client.security.CustomSslFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.util.SocketFactory;

public class MasterPatientIndexClientPdqFactory implements MasterPatientIndexClientFactory
{
	@Override
	public MasterPatientIndexClient getWebserviceClient(Function<String, String> propertyResolver)
	{
		String host = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.webservice.host");
		int port = Integer.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.webservice.port"));
		String keystorePath = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.path");
		String keystorePassword = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.password");

		String senderApplication = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.sender.application");
		String senderFacility = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.sender.facility");
		String receiverApplication =  propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.receiver.application");
		String receiverFacility = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.receiver.facility");

		String pidAssigningAuthorityNamespaceId = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.namespaceId");
		String pidAssigningAuthorityUniversalId = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.universalId");

		MessageHelper messageHelper = new MessageHelper();
		HapiContext context = new DefaultHapiContext();

		CustomSslFactory sslFactory = new CustomSslFactory();
		SocketFactory socketFactory = new CustomSocketFactory(keystorePath, keystorePassword, sslFactory);

		return new MasterPatientIndexClientPdq(host, port, senderApplication,
				senderFacility, receiverApplication, receiverFacility, pidAssigningAuthorityNamespaceId,
				pidAssigningAuthorityUniversalId, messageHelper, context, socketFactory);
	}
}
