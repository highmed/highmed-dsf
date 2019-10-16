package org.highmed.dsf.fhir.client;

import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.client.WebsocketClientTyrus;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class FhirClientProviderImpl implements FhirWebserviceClientProvider, FhirWebsocketClientProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirClientProviderImpl.class);

	private final Map<String, FhirWebserviceClient> webserviceClientsByUrl = new HashMap<>();
	private final Map<String, WebsocketClient> websocketClientsBySubscriptionId = new HashMap<>();

	private final FhirContext fhirContext;

	private final String localBaseUrl;
	private final int localReadTimeout;
	private final int localConnectTimeout;

	private final KeyStore webserviceTrustStore;
	private final KeyStore webserviceKeyStore;
	private final String webserviceKeyStorePassword;

	private final int remoteReadTimeout;
	private final int remoteConnectTimeout;
	private final String remoteProxyPassword;
	private final String remoteProxyUsername;
	private final String remoteProxySchemeHostPort;

	private final String localWebsocketUrl;
	private final KeyStore localWebsocketTrustStore;
	private final KeyStore localWebsocketKeyStore;
	private final String localWebsocketKeyStorePassword;

	public FhirClientProviderImpl(FhirContext fhirContext, String localBaseUrl, int localReadTimeout,
			int localConnectTimeout, KeyStore webserviceTrustStore, KeyStore webserviceKeyStore,
			String webserviceKeyStorePassword, int remoteReadTimeout, int remoteConnectTimeout,
			String remoteProxyPassword, String remoteProxyUsername, String remoteProxySchemeHostPort,
			String localWebsocketUrl, KeyStore localWebsocketTrustStore, KeyStore localWebsocketKeyStore,
			String localWebsocketKeyStorePassword)
	{
		this.fhirContext = fhirContext;
		this.localBaseUrl = localBaseUrl;
		this.localReadTimeout = localReadTimeout;
		this.localConnectTimeout = localConnectTimeout;
		this.webserviceTrustStore = webserviceTrustStore;
		this.webserviceKeyStore = webserviceKeyStore;
		this.webserviceKeyStorePassword = webserviceKeyStorePassword;
		this.remoteReadTimeout = remoteReadTimeout;
		this.remoteConnectTimeout = remoteConnectTimeout;
		this.remoteProxyPassword = remoteProxyPassword;
		this.remoteProxyUsername = remoteProxyUsername;
		this.remoteProxySchemeHostPort = remoteProxySchemeHostPort;
		this.localWebsocketUrl = localWebsocketUrl;
		this.localWebsocketTrustStore = localWebsocketTrustStore;
		this.localWebsocketKeyStore = localWebsocketKeyStore;
		this.localWebsocketKeyStorePassword = localWebsocketKeyStorePassword;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirContext, "fhirContext");
		Objects.requireNonNull(localBaseUrl, "localBaseUrl");
		if (localReadTimeout < 0)
			throw new IllegalArgumentException("localReadTimeout < 0");
		if (localConnectTimeout < 0)
			throw new IllegalArgumentException("localConnectTimeout < 0");
		Objects.requireNonNull(webserviceTrustStore, "webserviceTrustStore");
		Objects.requireNonNull(webserviceKeyStore, "webserviceKeyStore");
		Objects.requireNonNull(webserviceKeyStorePassword, "webserviceKeyStorePassword");
		if (remoteReadTimeout < 0)
			throw new IllegalArgumentException("remoteReadTimeout < 0");
		if (remoteConnectTimeout < 0)
			throw new IllegalArgumentException("remoteConnectTimeout < 0");
		if (remoteProxyPassword == null)
			logger.info("remoteProxyPassword is null");
		if (remoteProxyUsername == null)
			logger.info("remoteProxyUsername is null");
		if (remoteProxySchemeHostPort == null)
			logger.info("remoteProxySchemeHostPort is null");
		Objects.requireNonNull(localWebsocketUrl, "localWebsocketUrl");
		Objects.requireNonNull(localWebsocketTrustStore, "localWebsocketTrustStore");
		Objects.requireNonNull(localWebsocketKeyStore, "localWebsocketKeyStore");
		Objects.requireNonNull(localWebsocketKeyStorePassword, "localWebsocketKeyStorePassword");
	}

	public String getLocalBaseUrl()
	{
		return localBaseUrl;
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
				if (localBaseUrl.equals(webserviceUrl))
					client = new FhirWebserviceClientJersey(webserviceUrl, webserviceTrustStore, webserviceKeyStore,
							webserviceKeyStorePassword, null, null, null, localConnectTimeout, localReadTimeout, null,
							fhirContext);
				else
					client = new FhirWebserviceClientJersey(webserviceUrl, webserviceTrustStore, webserviceKeyStore,
							webserviceKeyStorePassword, remoteProxySchemeHostPort, remoteProxyUsername,
							remoteProxyPassword, remoteConnectTimeout, remoteReadTimeout, null, fhirContext);

				webserviceClientsByUrl.put(webserviceUrl, client);
				return client;
			}
		}
	}

	@Override
	public FhirWebserviceClient getLocalWebserviceClient()
	{
		return getRemoteWebserviceClient(localBaseUrl);
	}

	@Override
	public FhirWebserviceClient getRemoteWebserviceClient(String webserviceUrl)
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
	public FhirWebserviceClient getRemoteWebserviceClient(IdType organizationReference)
	{
		Objects.requireNonNull(organizationReference, "organizationReference");
		if (organizationReference.hasBaseUrl())
			throw new IllegalArgumentException("Reference to locally stored organization expected");

		Endpoint endpoint = searchForEndpoint("_id", organizationReference.getIdPart());
		return getRemoteWebserviceClient(endpoint.getAddress());
	}

	private Endpoint searchForEndpoint(String searchParameter, String searchParameterValue)
	{
		Bundle resultSet = getLocalWebserviceClient().search(Organization.class,
				Map.of(searchParameter, Collections.singletonList(searchParameterValue), "_include",
						Collections.singletonList("Organization:endpoint")));

		if (resultSet.getTotal() != 1 || resultSet.getEntry().size() != 2)
			throw new IllegalStateException(
					"Resultset with total 1 and 2 entries expected, but got (" + resultSet.getTotal() + "/" + resultSet
							.getEntry().size() + ")");

		final Endpoint endpoint;
		if (resultSet.getEntry().get(1).getResource() instanceof Endpoint)
			endpoint = (Endpoint) resultSet.getEntry().get(1).getResource();
		else if (resultSet.getEntry().get(0).getResource() instanceof Endpoint)
			endpoint = (Endpoint) resultSet.getEntry().get(1).getResource();
		else
			endpoint = null;

		if (endpoint == null)
			throw new IllegalStateException(
					"Resultset missing resource of type " + Endpoint.class.getName() + ", found (" + resultSet
							.getEntry().stream().map(e -> e.getResource().getClass().getName())
							.collect(Collectors.joining(", ")) + ")");
		return endpoint;
	}

	@Override
	public FhirWebserviceClient getRemoteWebserviceClient(String organizationIdentifierSystem,
			String organizationIdentifierValue)
	{
		Objects.requireNonNull(organizationIdentifierSystem, "organizationIdentifierSystem");
		Objects.requireNonNull(organizationIdentifierValue, "organizationIdentifierValue");

		Endpoint endpoint = searchForEndpoint("identifier",
				organizationIdentifierSystem + "|" + organizationIdentifierValue);
		return getRemoteWebserviceClient(endpoint.getAddress());
	}

	@Override
	public WebsocketClient getLocalWebsocketClient(String subscriptionId)
	{
		if (!websocketClientsBySubscriptionId.containsKey(subscriptionId))
		{
			WebsocketClientTyrus client = new WebsocketClientTyrus(fhirContext, URI.create(localWebsocketUrl),
					localWebsocketTrustStore, localWebsocketKeyStore, localWebsocketKeyStorePassword, subscriptionId);
			websocketClientsBySubscriptionId.put(subscriptionId, client);
			return client;

		}

		return websocketClientsBySubscriptionId.get(subscriptionId);
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
