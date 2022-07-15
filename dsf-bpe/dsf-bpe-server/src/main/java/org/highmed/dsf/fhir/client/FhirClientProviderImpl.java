package org.highmed.dsf.fhir.client;

import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class FhirClientProviderImpl
		implements FhirWebserviceClientProvider, FhirWebsocketClientProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirClientProviderImpl.class);

	private final Map<String, FhirWebserviceClient> webserviceClientsByUrl = new HashMap<>();
	private final Map<String, WebsocketClient> websocketClientsBySubscriptionId = new HashMap<>();

	private final FhirContext fhirContext;
	private final ReferenceCleaner referenceCleaner;

	private final String localWebserviceBaseUrl;
	private final int localWebserviceReadTimeout;
	private final int localWebserviceConnectTimeout;
	private final String localWebserviceProxySchemeHostPort;
	private final String localWebserviceProxyUsername;
	private final char[] localWebserviceProxyPassword;
	private final boolean localWebserviceLogRequests;

	private final KeyStore webserviceTrustStore;
	private final KeyStore webserviceKeyStore;
	private final char[] webserviceKeyStorePassword;

	private final int remoteWebserviceReadTimeout;
	private final int remoteWebserviceConnectTimeout;
	private final String remoteWebserviceProxySchemeHostPort;
	private final String remoteWebserviceProxyUsername;
	private final char[] remoteWebserviceProxyPassword;
	private final boolean remoteWebserviceLogRequests;

	private final String localWebsocketUrl;
	private final KeyStore localWebsocketTrustStore;
	private final KeyStore localWebsocketKeyStore;
	private final char[] localWebsocketKeyStorePassword;

	private final String localWebsocketProxySchemeHostPort;
	private final String localWebsocketProxyUsername;
	private final char[] localWebsocketProxyPassword;

	public FhirClientProviderImpl(FhirContext fhirContext, ReferenceCleaner referenceCleaner,
			String localWebserviceBaseUrl, int localWebserviceReadTimeout, int localWebserviceConnectTimeout,
			String localWebserviceProxySchemeHostPort, String localWebserviceProxyUsername,
			char[] localWebserviceProxyPassword, boolean localWebserviceLogRequests, KeyStore webserviceTrustStore,
			KeyStore webserviceKeyStore, char[] webserviceKeyStorePassword, int remoteWebserviceReadTimeout,
			int remoteWebserviceConnectTimeout, String remoteWebserviceProxySchemeHostPort,
			String remoteWebserviceProxyUsername, char[] remoteWebserviceProxyPassword,
			boolean remoteWebserviceLogRequests, String localWebsocketUrl, KeyStore localWebsocketTrustStore,
			KeyStore localWebsocketKeyStore, char[] localWebsocketKeyStorePassword,
			String localWebsocketProxySchemeHostPort, String localWebsocketProxyUsername,
			char[] localWebsocketProxyPassword)
	{
		this.fhirContext = fhirContext;
		this.referenceCleaner = referenceCleaner;

		this.localWebserviceBaseUrl = localWebserviceBaseUrl;
		this.localWebserviceReadTimeout = localWebserviceReadTimeout;
		this.localWebserviceConnectTimeout = localWebserviceConnectTimeout;
		this.localWebserviceProxySchemeHostPort = localWebserviceProxySchemeHostPort;
		this.localWebserviceProxyUsername = localWebserviceProxyUsername;
		this.localWebserviceProxyPassword = localWebserviceProxyPassword;
		this.localWebserviceLogRequests = localWebserviceLogRequests;

		this.webserviceTrustStore = webserviceTrustStore;
		this.webserviceKeyStore = webserviceKeyStore;
		this.webserviceKeyStorePassword = webserviceKeyStorePassword;

		this.remoteWebserviceReadTimeout = remoteWebserviceReadTimeout;
		this.remoteWebserviceConnectTimeout = remoteWebserviceConnectTimeout;
		this.remoteWebserviceProxySchemeHostPort = remoteWebserviceProxySchemeHostPort;
		this.remoteWebserviceProxyUsername = remoteWebserviceProxyUsername;
		this.remoteWebserviceProxyPassword = remoteWebserviceProxyPassword;
		this.remoteWebserviceLogRequests = remoteWebserviceLogRequests;

		this.localWebsocketUrl = localWebsocketUrl;
		this.localWebsocketTrustStore = localWebsocketTrustStore;
		this.localWebsocketKeyStore = localWebsocketKeyStore;
		this.localWebsocketKeyStorePassword = localWebsocketKeyStorePassword;
		this.localWebsocketProxySchemeHostPort = localWebsocketProxySchemeHostPort;
		this.localWebsocketProxyUsername = localWebsocketProxyUsername;
		this.localWebsocketProxyPassword = localWebsocketProxyPassword;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirContext, "fhirContext");
		Objects.requireNonNull(localWebserviceBaseUrl, "localBaseUrl");
		if (localWebserviceReadTimeout < 0)
			throw new IllegalArgumentException("localReadTimeout < 0");
		if (localWebserviceConnectTimeout < 0)
			throw new IllegalArgumentException("localConnectTimeout < 0");
		Objects.requireNonNull(webserviceTrustStore, "webserviceTrustStore");
		Objects.requireNonNull(webserviceKeyStore, "webserviceKeyStore");
		Objects.requireNonNull(webserviceKeyStorePassword, "webserviceKeyStorePassword");
		if (remoteWebserviceReadTimeout < 0)
			throw new IllegalArgumentException("remoteReadTimeout < 0");
		if (remoteWebserviceConnectTimeout < 0)
			throw new IllegalArgumentException("remoteConnectTimeout < 0");
		Objects.requireNonNull(localWebsocketUrl, "localWebsocketUrl");
		Objects.requireNonNull(localWebsocketTrustStore, "localWebsocketTrustStore");
		Objects.requireNonNull(localWebsocketKeyStore, "localWebsocketKeyStore");
		Objects.requireNonNull(localWebsocketKeyStorePassword, "localWebsocketKeyStorePassword");
	}

	public String getLocalBaseUrl()
	{
		return localWebserviceBaseUrl;
	}

	private FhirWebserviceClient getClient(String webserviceUrl)
	{
		synchronized (webserviceClientsByUrl)
		{
			if (webserviceClientsByUrl.containsKey(webserviceUrl))
				return webserviceClientsByUrl.get(webserviceUrl);
			else
			{
				FhirWebserviceClient client;
				if (localWebserviceBaseUrl.equals(webserviceUrl))
					client = new FhirWebserviceClientJersey(webserviceUrl, webserviceTrustStore, webserviceKeyStore,
							webserviceKeyStorePassword, localWebserviceProxySchemeHostPort,
							localWebserviceProxyUsername, localWebserviceProxyPassword, localWebserviceConnectTimeout,
							localWebserviceReadTimeout, localWebserviceLogRequests, null, fhirContext,
							referenceCleaner);
				else
					client = new FhirWebserviceClientJersey(webserviceUrl, webserviceTrustStore, webserviceKeyStore,
							webserviceKeyStorePassword, remoteWebserviceProxySchemeHostPort,
							remoteWebserviceProxyUsername, remoteWebserviceProxyPassword,
							remoteWebserviceConnectTimeout, remoteWebserviceReadTimeout, remoteWebserviceLogRequests,
							null, fhirContext, referenceCleaner);

				webserviceClientsByUrl.put(webserviceUrl, client);
				return client;
			}
		}
	}

	@Override
	public FhirWebserviceClient getLocalWebserviceClient()
	{
		return getWebserviceClient(localWebserviceBaseUrl);
	}

	@Override
	public FhirWebserviceClient getWebserviceClient(String webserviceUrl)
	{
		Objects.requireNonNull(webserviceUrl, "webserviceUrl");

		FhirWebserviceClient cachedClient = webserviceClientsByUrl.get(webserviceUrl);
		if (cachedClient != null)
			return cachedClient;
		else
		{
			FhirWebserviceClient newClient = getClient(webserviceUrl);
			webserviceClientsByUrl.put(webserviceUrl, newClient);
			return newClient;
		}
	}

	@Override
	public WebsocketClient getLocalWebsocketClient(Runnable reconnector, String subscriptionId)
	{
		if (!websocketClientsBySubscriptionId.containsKey(subscriptionId))
		{
			WebsocketClientTyrus client = createWebsocketClient(reconnector, subscriptionId);
			websocketClientsBySubscriptionId.put(subscriptionId, client);
			return client;
		}

		return websocketClientsBySubscriptionId.get(subscriptionId);
	}

	protected WebsocketClientTyrus createWebsocketClient(Runnable reconnector, String subscriptionId)
	{
		return new WebsocketClientTyrus(reconnector, URI.create(localWebsocketUrl), localWebsocketTrustStore,
				localWebsocketKeyStore, localWebsocketKeyStorePassword, localWebsocketProxySchemeHostPort,
				localWebsocketProxyUsername, localWebsocketProxyPassword, subscriptionId);
	}

	@Override
	public void disconnectAll()
	{
		for (WebsocketClient c : websocketClientsBySubscriptionId.values())
		{
			try
			{
				c.disconnect();
			}
			catch (Exception e)
			{
				logger.warn("Error while disconnecting websocket client", e);
			}
		}
	}
}
