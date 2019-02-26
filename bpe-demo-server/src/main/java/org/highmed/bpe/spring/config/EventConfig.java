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

import org.highmed.bpe.event.EventHandler;
import org.highmed.bpe.event.EventType;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

@Configuration
public class EventConfig
{
	@Value("${org.highmed.bpe.fhir.websocket.url}")
	private String websocketUrl;
	
	@Value("${org.highmed.bpe.fhir.websocket.keystore.p12file}")
	private String websocketKeyStoreFile;
	
	@Value("${org.highmed.bpe.fhir.websocket.keystore.password}")
	private String websocketKeyStorePassword;
	
	@Value("${org.highmed.bpe.fhir.websocket.subscription.id}")
	private String subscriptionIdPart;

	@Value("${org.highmed.bpe.fhir.websocket.subscription.payload}")
	private String subscriptionPayloadType;

	@Autowired
	private FhirConfig fhirConfig;

	
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

			return new WebsocketClientTyrus(fhirConfig.fhirContext(), URI.create(websocketUrl), trustStore, keyStore, websocketKeyStorePassword, subscriptionIdPart);
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
		return new EventHandler(fhirWebsocketClient(), fhirConfig.fhirContext(), eventType);
	}
}
