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

import org.highmed.fhir.client.WebserviceClient;
import org.highmed.fhir.client.WebserviceClientJersey;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.highmed.fhir.task.TaskHandler;
import org.highmed.fhir.variables.DomainResourceSerializer;
import org.highmed.fhir.variables.FhirPlugin;
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
	@Value("${org.highmed.bpe.fhir.webservice.url}")
	private String webserviceUrl;

	@Value("${org.highmed.bpe.fhir.webservice.keystore.p12file}")
	private String webserviceKeyStoreFile;

	@Value("${org.highmed.bpe.fhir.webservice.keystore.password}")
	private String webserviceKeyStorePassword;

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

	@Bean
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	public FhirPlugin fhirPlugin()
	{
		return new FhirPlugin(domainResourceSerializer());
	}

	@Bean
	public DomainResourceSerializer domainResourceSerializer()
	{
		return new DomainResourceSerializer(fhirContext());
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
	public WebserviceClient fhirWebserviceClient()
	{
		try
		{
			Path ksFile = Paths.get(webserviceKeyStoreFile);

			if (!Files.isReadable(ksFile))
				throw new IOException("Webservice keystore file '" + ksFile.toString() + "' not readable");

			KeyStore keyStore = CertificateReader.fromPkcs12(ksFile, webserviceKeyStorePassword);
			KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

			return new WebserviceClientJersey(webserviceUrl, trustStore, keyStore, webserviceKeyStorePassword, null,
					null, null, 5_000, 10_000, null, fhirContext());
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
						fhirWebserviceClient(), subscriptionIdPart, searchCriteria);
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
				fhirWebserviceClient());
	}
}
