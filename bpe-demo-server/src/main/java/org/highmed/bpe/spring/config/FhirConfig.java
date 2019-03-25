package org.highmed.bpe.spring.config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.highmed.fhir.client.ClientProvider;
import org.highmed.fhir.client.ClientProviderImpl;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.highmed.fhir.task.TaskHandler;
import org.highmed.fhir.variables.DomainResourceSerializer;
import org.highmed.fhir.variables.FhirPlugin;
import org.highmed.fhir.variables.MultiInstanceTargetSerializer;
import org.highmed.fhir.variables.MultiInstanceTargetsSerializer;
import org.highmed.fhir.websocket.EventHandler;
import org.highmed.fhir.websocket.EventType;
import org.highmed.fhir.websocket.LastEventTimeIo;
import org.highmed.fhir.websocket.PingEventHandler;
import org.highmed.fhir.websocket.ResourceEventHandler;
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

	@Value("${org.highmed.bpe.fhir.webservice.keystore.p12file}")
	private String webserviceKeyStoreFile;

	@Value("${org.highmed.bpe.fhir.webservice.keystore.password}")
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

	@Value("${org.highmed.bpe.fhir.local.webservice.url}")
	private String localWebserviceUrl;

	@Value("${org.highmed.bpe.fhir.local.webservice.readTimeout}")
	private int localReadTimeout;

	@Value("${org.highmed.bpe.fhir.local.webservice.connectTimeout}")
	private int localConnectTimeout;

	@Value("${org.highmed.bpe.fhir.websocket.url}")
	private String websocketUrl;

	@Value("${org.highmed.bpe.fhir.websocket.keystore.p12file}")
	private String websocketKeyStoreFile;

	@Value("${org.highmed.bpe.fhir.websocket.keystore.password}")
	private String websocketKeyStorePassword;

	@Value("${org.highmed.bpe.fhir.task.subscription.id}")
	private String subscriptionIdPart;

	@Value("${org.highmed.bpe.fhir.task.subscription.payload}")
	private String subscriptionPayloadType;

	@Value("${org.highmed.bpe.fhir.task.subscription.last_event_time_file}")
	private String lastEventTimeFile;

	@Value("${org.highmed.bpe.fhir.task.subscription.criteria:#{null}}")
	private String searchCriteria;

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
	public WebsocketClient fhirWebsocketClient()
	{
		try
		{
			Path ksFile = Paths.get(websocketKeyStoreFile);

			if (!Files.isReadable(ksFile))
				throw new IOException("Websocket keystore file '" + ksFile.toString() + "' not readable");

			KeyStore keyStore = CertificateReader.fromPkcs12(ksFile, websocketKeyStorePassword);
			KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

			return new WebsocketClientTyrus(fhirContext(), URI.create(websocketUrl), trustStore, keyStore,
					websocketKeyStorePassword, subscriptionIdPart);
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	public EventHandler eventHandler()
	{
		EventType eventType = EventType.fromString(subscriptionPayloadType);

		switch (eventType)
		{
			case JSON:
			case XML:
				return new ResourceEventHandler(fhirWebsocketClient(), taskHandler(), eventType, fhirContext());
			case PING:
				return new PingEventHandler(fhirWebsocketClient(), lastEventTimeIo(), taskHandler(),
						clientProvider().getLocalClient(), subscriptionIdPart, searchCriteria);
			default:
				throw new IllegalArgumentException("Unknonw event type: " + eventType);
		}
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
				clientProvider().getLocalClient());
	}

	@Bean
	public ClientProvider clientProvider()
	{
		try
		{
			Path ksFile = Paths.get(webserviceKeyStoreFile);

			if (!Files.isReadable(ksFile))
				throw new IOException("Webservice keystore file '" + ksFile.toString() + "' not readable");

			KeyStore keyStore = CertificateReader.fromPkcs12(ksFile, webserviceKeyStorePassword);
			KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

			return new ClientProviderImpl(fhirContext(), keyStore, websocketKeyStorePassword, trustStore,
					remoteReadTimeout, remoteConnectTimeout, remoteProxyPassword, remoteProxyUsername,
					remoteProxySchemeHostPort, localReadTimeout, localConnectTimeout, localWebserviceUrl);
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
