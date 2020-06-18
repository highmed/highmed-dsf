package org.highmed.mpi.client.pdq;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.security.CustomSocketFactory;
import org.highmed.mpi.client.security.CustomSslFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.util.SocketFactory;

public class MasterPatientIndexClientPdqFactory implements MasterPatientIndexClientFactory
{
	@Override
	public MasterPatientIndexClient getWebserviceClient(Environment environment)
	{
		Properties properties = extractPropertiesFromEnvironment(environment);
		return getWebserviceClient(properties);
	}

	@Override
	public MasterPatientIndexClient getWebserviceClient(Properties properties)
	{
		String host = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.webservice.host");
		int port = Integer.parseInt(properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.webservice.port"));
		String keystorePath = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.path");
		String keystorePassword = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.webservice.keystore.password");

		String senderApplication = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.sender.application");
		String senderFacility = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.sender.facility");
		String receiverApplication =  properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.receiver.application");
		String receiverFacility = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.receiver.facility");

		String pidAssigningAuthorityNamespaceId = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.namespaceId");
		String pidAssigningAuthorityUniversalId = properties.getProperty("org.highmed.dsf.bpe.mpi.pdq.assigningAuthority.universalId");

		MessageHelper messageHelper = new MessageHelper();
		HapiContext context = new DefaultHapiContext();

		CustomSslFactory sslFactory = new CustomSslFactory();
		SocketFactory socketFactory = new CustomSocketFactory(keystorePath, keystorePassword, sslFactory);

		return new MasterPatientIndexClientPdq(host, port, senderApplication,
				senderFacility, receiverApplication, receiverFacility, pidAssigningAuthorityNamespaceId,
				pidAssigningAuthorityUniversalId, messageHelper, context, socketFactory);
	}

	private Properties extractPropertiesFromEnvironment(Environment springEnv)
	{
		Properties properties = new Properties();
		MutablePropertySources propertySources = ((AbstractEnvironment) springEnv).getPropertySources();
		StreamSupport.stream(propertySources.spliterator(), false)
				.filter(ps -> ps instanceof EnumerablePropertySource)
				.map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
				.flatMap(Arrays::<String>stream)
				.forEach(propName -> properties.setProperty(propName, springEnv.getProperty(propName)));

		return properties;
	}
}
