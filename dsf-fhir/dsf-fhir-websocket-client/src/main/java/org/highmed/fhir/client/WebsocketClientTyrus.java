package org.highmed.fhir.client;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientManager.ReconnectHandler;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class WebsocketClientTyrus implements WebsocketClient
{
	private static final Logger logger = LoggerFactory.getLogger(WebsocketClientTyrus.class);

	private final ReconnectHandler reconnectHandler = new ReconnectHandler()
	{
		@Override
		public boolean onConnectFailure(Exception exception)
		{
			logger.warn("onConnectFailure {}: {}", exception.getClass().getName(), exception.getMessage());
			logger.debug("onConnectFailure", exception);
			return true;
		}

		@Override
		public boolean onDisconnect(CloseReason closeReason)
		{
			logger.warn("OnDisconnect {}", closeReason.getReasonPhrase());
			return !closed;
		}
	};

	private final URI wsUri;
	private final SSLContext sslContext;
	private final ClientEndpoint endpoint;

	private ClientManager manager;
	private Session connection;
	private volatile boolean closed;

	public WebsocketClientTyrus(FhirContext fhirContext, URI wsUri, KeyStore trustStore, KeyStore keyStore,
			String keyStorePassword, String subscriptionIdPart)
	{
		this.wsUri = wsUri;

		if (trustStore != null && keyStore == null && keyStorePassword == null)
			sslContext = SslConfigurator.newInstance().trustStore(trustStore).createSSLContext();
		else if (trustStore != null && keyStore != null && keyStorePassword != null)
			sslContext = SslConfigurator.newInstance().trustStore(trustStore).keyStore(keyStore)
					.keyStorePassword(keyStorePassword).createSSLContext();
		else
			sslContext = SslConfigurator.getDefaultContext();

		this.endpoint = new ClientEndpoint(subscriptionIdPart);
	}

	@Override
	public void connect()
	{
		if (manager != null)
			throw new IllegalStateException("Allready connecting/connected");

		manager = ClientManager.createClient();
		manager.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
		manager.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, new SslEngineConfigurator(sslContext));

		try
		{
			logger.debug("Connecting to websocket {} and waiting for connection", wsUri);
			connection = manager.connectToServer(endpoint, wsUri);
		}
		catch (DeploymentException e)
		{
			logger.warn("Error while connecting to server", e);
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			logger.warn("Error while connecting to server", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void disconnect()
	{
		if (closed)
			return;

		logger.debug("Closing websocket {}", wsUri);
		try
		{
			connection.close();
			connection = null;
		}
		catch (IOException e)
		{
			logger.warn("Error while closing websocket", e);
		}

		manager.shutdown();
		manager = null;

		closed = true;
	}

	@Override
	public void setDomainResourceHandler(Consumer<DomainResource> handler, Supplier<IParser> parserFactory)
	{
		endpoint.setDomainResourceHandler(handler, parserFactory);
	}

	@Override
	public void setPingHandler(Consumer<String> handler)
	{
		endpoint.setPingHandler(handler);
	}
}
