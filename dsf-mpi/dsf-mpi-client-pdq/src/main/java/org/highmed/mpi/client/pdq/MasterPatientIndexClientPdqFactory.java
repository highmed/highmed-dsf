package org.highmed.mpi.client.pdq;

import java.util.Optional;
import java.util.function.BiFunction;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.security.CustomSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.util.SocketFactory;

public class MasterPatientIndexClientPdqFactory implements MasterPatientIndexClientFactory
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexClientPdqFactory.class);

	@Override
	public MasterPatientIndexClient createClient(BiFunction<String, String, String> propertyResolver)
	{
		String host = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.host", null);
		int port = Integer.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.port", "-1"));

		String trustCertificatesFile = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.trust.certificates", null);
		String clientCertificateFile = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.client.certificate", null);
		String clientCertificatePrivateKeyFile = propertyResolver
				.apply("org.highmed.dsf.bpe.mpi.pdq.client.certificate.private.key", null);
		char[] clientCertificatePrivateKeyPassword = Optional
				.ofNullable(propertyResolver
						.apply("org.highmed.dsf.bpe.mpi.pdq.client.certificate.private.key.password", null))
				.map(String::toCharArray).orElse(null);

		String senderApplication = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.sender.application", null);
		String senderFacility = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.sender.facility", null);
		String receiverApplication = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.receiver.application", null);
		String receiverFacility = propertyResolver.apply("org.highmed.dsf.bpe.mpi.pdq.receiver.facility", null);

		String pidAssigningAuthorityNamespaceId = propertyResolver
				.apply("org.highmed.dsf.bpe.mpi.pdq.assigning.authority.id.namespace", null);
		String pidAssigningAuthorityUniversalId = propertyResolver
				.apply("org.highmed.dsf.bpe.mpi.pdq.assigning.authority.id.universal", null);

		MessageHelper messageHelper = new MessageHelper();
		HapiContext context = new DefaultHapiContext();

		SocketFactory socketFactory = null;

		if (trustCertificatesFile != null && !trustCertificatesFile.isBlank() && clientCertificateFile != null
				&& !clientCertificateFile.isBlank() && clientCertificatePrivateKeyFile != null
				&& !clientCertificatePrivateKeyFile.isBlank())
		{
			logger.debug(
					"Using CustomSocketFactory with trustCertificatesFile={}, clientCertificateFile={} and clientCertificatePrivateKeyFile={}",
					trustCertificatesFile, clientCertificateFile, clientCertificatePrivateKeyFile);
			socketFactory = new CustomSocketFactory(trustCertificatesFile, clientCertificateFile,
					clientCertificatePrivateKeyFile, clientCertificatePrivateKeyPassword);
		}

		return new MasterPatientIndexClientPdq(host, port, senderApplication, senderFacility, receiverApplication,
				receiverFacility, pidAssigningAuthorityNamespaceId, pidAssigningAuthorityUniversalId, messageHelper,
				context, socketFactory);
	}
}
