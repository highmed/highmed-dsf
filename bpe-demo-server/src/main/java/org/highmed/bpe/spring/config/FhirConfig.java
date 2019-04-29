package org.highmed.bpe.spring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.highmed.fhir.client.ClientProviderImpl;
import org.highmed.fhir.client.WebsocketClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.organization.OrganizationProviderImpl;
import org.highmed.fhir.task.TaskHandler;
import org.highmed.fhir.variables.DomainResourceSerializer;
import org.highmed.fhir.variables.FhirPlugin;
import org.highmed.fhir.variables.MultiInstanceTargetSerializer;
import org.highmed.fhir.variables.MultiInstanceTargetsSerializer;
import org.highmed.fhir.websocket.FhirConnector;
import org.highmed.fhir.websocket.LastEventTimeIo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

@Configuration
public class FhirConfig
{
	@Value("${org.highmed.bpe.fhir.organization.identifier.codeSystem}")
	private String organizationIdentifierCodeSystem;

	@Value("${org.highmed.bpe.fhir.organization.identifier.localValue}")
	private String organizationIdentifierLocalValue;

	@Value("${org.highmed.bpe.fhir.local.webservice.keystore.p12file}")
	private String webserviceKeyStoreFile;

	@Value("${org.highmed.bpe.fhir.local.webservice.keystore.password}")
	private String webserviceKeyStorePassword;

	@Value("${org.highmed.bpe.fhir.remote.webservice.readTimeout}")
	private int remoteReadTimeout;

	@Value("${org.highmed.bpe.fhir.remote.webservice.connectTimeout}")
	private int remoteConnectTimeout;

	@Value("${org.highmed.bpe.fhir.remote.webservice.proxy.password:#{null}}")
	private String remoteProxyPassword;

	@Value("${org.highmed.bpe.fhir.remote.webservice.proxy.username:#{null}}")
	private String remoteProxyUsername;

	@Value("${org.highmed.bpe.fhir.remote.webservice.proxy.schemeHostPort:#{null}}")
	private String remoteProxySchemeHostPort;

	@Value("${org.highmed.bpe.fhir.local.webservice.baseUrl}")
	private String localWebserviceBaseUrl;

	@Value("${org.highmed.bpe.fhir.local.webservice.readTimeout}")
	private int localReadTimeout;

	@Value("${org.highmed.bpe.fhir.local.webservice.connectTimeout}")
	private int localConnectTimeout;

	@Value("${org.highmed.bpe.fhir.local.websocket.url}")
	private String localWebsocketUrl;

	@Value("${org.highmed.bpe.fhir.local.websocket.keystore.p12file}")
	private String localWebsocketKeyStoreFile;

	@Value("${org.highmed.bpe.fhir.local.websocket.keystore.password}")
	private String localWebsocketKeyStorePassword;

	@Value("${org.highmed.bpe.fhir.task.subscription.searchParameter}")
	private String subscriptionSearchParameter;

	@Value("${org.highmed.bpe.fhir.task.subscription.lastEventTimeFile}")
	private String lastEventTimeFile;

	@Autowired
	private CamundaConfig camundaConfig;

	@Autowired
	private JsonConfig jsonConfig;

	@Bean
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	public FhirPlugin fhirPlugin()
	{
		return new FhirPlugin(domainResourceSerializer(), multiInstanceTargetSerializer(),
				multiInstanceTargetsSerializer());
	}

	@Bean
	public DomainResourceSerializer domainResourceSerializer()
	{
		return new DomainResourceSerializer(fhirContext());
	}

	@Bean
	public MultiInstanceTargetSerializer multiInstanceTargetSerializer()
	{
		return new MultiInstanceTargetSerializer(jsonConfig.objectMapper());
	}

	@Bean
	public MultiInstanceTargetsSerializer multiInstanceTargetsSerializer()
	{
		return new MultiInstanceTargetsSerializer(jsonConfig.objectMapper());
	}

	@Bean
	public LastEventTimeIo lastEventTimeIo()
	{
		return new LastEventTimeIo(Paths.get(lastEventTimeFile));
	}

	@Bean
	public TaskHandler taskHandler()
	{
		return new TaskHandler(camundaConfig.runtimeService(), camundaConfig.repositoryService(),
				clientProvider().getLocalWebserviceClient());
	}

	@Bean
	public WebsocketClientProvider clientProvider()
	{
		try
		{
			Path localWebserviceKsFile = Paths.get(webserviceKeyStoreFile);

			if (!Files.isReadable(localWebserviceKsFile))
				throw new IOException(
						"Webservice keystore file '" + localWebserviceKsFile.toString() + "' not readable");

			KeyStore localWebserviceKeyStore = CertificateReader.fromPkcs12(localWebserviceKsFile,
					webserviceKeyStorePassword);
			KeyStore localWebserviceTrustStore = CertificateHelper.extractTrust(localWebserviceKeyStore);

			Path localWebsocketKsFile = Paths.get(localWebsocketKeyStoreFile);

			if (!Files.isReadable(localWebsocketKsFile))
				throw new IOException("Websocket keystore file '" + localWebsocketKsFile.toString() + "' not readable");

			KeyStore localWebsocketKeyStore = CertificateReader.fromPkcs12(localWebsocketKsFile,
					localWebsocketKeyStorePassword);
			KeyStore localWebsocketTrustStore = CertificateHelper.extractTrust(localWebsocketKeyStore);

			return new ClientProviderImpl(fhirContext(), localWebserviceBaseUrl, localReadTimeout, localConnectTimeout,
					localWebserviceTrustStore, localWebserviceKeyStore, webserviceKeyStorePassword, remoteReadTimeout,
					remoteConnectTimeout, remoteProxyPassword, remoteProxyUsername, remoteProxySchemeHostPort,
					localWebsocketUrl, localWebsocketTrustStore, localWebsocketKeyStore,
					localWebsocketKeyStorePassword);
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	public OrganizationProvider organizationProvider()
	{
		return new OrganizationProviderImpl(clientProvider(), organizationIdentifierCodeSystem,
				organizationIdentifierLocalValue);
	}

	@Bean
	public FhirConnector fhirConnector()
	{
		return new FhirConnector(clientProvider(), taskHandler(), lastEventTimeIo(), fhirContext(),
				subscriptionSearchParameter);
	}
}
