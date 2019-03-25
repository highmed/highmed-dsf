package org.highmed.fhir.client;

import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

public class ClientProviderImpl implements ClientProvider
{
	private final Map<String, WebserviceClient> clientsByUrl = new HashMap<>();

	private final FhirContext fhirContext;

	private final KeyStore keyStore;
	private final String keyStorePassword;
	private final KeyStore trustStore;

	private final int remoteReadTimeout;
	private final int remoteConnectTimeout;
	private final String remoteProxyPassword;
	private final String remoteProxyUsername;
	private final String remoteProxySchemeHostPort;

	private final int localReadTimeout;
	private final int localConnectTimeout;
	private final String localBaseUrl;

	public ClientProviderImpl(FhirContext fhirContext, KeyStore keyStore, String keyStorePassword, KeyStore trustStore,
			int remoteReadTimeout, int remoteConnectTimeout, String remoteProxyPassword, String remoteProxyUsername,
			String remoteProxySchemeHostPort, int localReadTimeout, int localConnectTimeout, String localWebserviceUrl)
	{
		this.fhirContext = fhirContext;
		this.keyStore = keyStore;
		this.keyStorePassword = keyStorePassword;
		this.trustStore = trustStore;
		this.remoteReadTimeout = remoteReadTimeout;
		this.remoteConnectTimeout = remoteConnectTimeout;
		this.remoteProxyPassword = remoteProxyPassword;
		this.remoteProxyUsername = remoteProxyUsername;
		this.remoteProxySchemeHostPort = remoteProxySchemeHostPort;
		this.localReadTimeout = localReadTimeout;
		this.localConnectTimeout = localConnectTimeout;
		this.localBaseUrl = localWebserviceUrl;
	}

	private WebserviceClient getClient(String webserviceUrl)
	{
		synchronized (clientsByUrl)
		{
			if (clientsByUrl.containsKey(webserviceUrl))
				return clientsByUrl.get(webserviceUrl);
			else
			{
				WebserviceClient client;
				if (localBaseUrl.equals(webserviceUrl))
					client = new WebserviceClientJersey(webserviceUrl, trustStore, keyStore, keyStorePassword, null, null,
							null, localConnectTimeout, localReadTimeout, null, fhirContext);
				else
					client = new WebserviceClientJersey(webserviceUrl, trustStore, keyStore, keyStorePassword,
							remoteProxySchemeHostPort, remoteProxyUsername, remoteProxyPassword, remoteConnectTimeout,
							remoteReadTimeout, null, fhirContext);

				clientsByUrl.put(webserviceUrl, client);
				return client;
			}
		}
	}

	@Override
	public WebserviceClient getLocalClient()
	{
		return getRemoteClient(localBaseUrl);
	}

	@Override
	public WebserviceClient getRemoteClient(String webserviceUrl)
	{
		WebserviceClient cachedClient = clientsByUrl.get(webserviceUrl);
		if (cachedClient != null)
			return cachedClient;
		else
		{
			WebserviceClient newClient = getClient(webserviceUrl);
			clientsByUrl.put(webserviceUrl, newClient);
			return newClient;
		}
	}

	@Override
	public WebserviceClient getRemoteClient(IdType organizationReference)
	{
		if (organizationReference.hasBaseUrl())
			throw new IllegalArgumentException("Reference to locally stored organization expected");

		Bundle resultSet = getLocalClient().search(Organization.class,
				Map.of("_id", Collections.singletonList(organizationReference.getIdPart()), "_include",
						Collections.singletonList("Organization:endpoint")));

		if (resultSet.getTotal() != 1 || resultSet.getEntry().size() != 2)
			throw new IllegalStateException("Resultset with total 1 and 2 entries expected, but got ("
					+ resultSet.getTotal() + "/" + resultSet.getEntry().size() + ")");

		final Endpoint endpoint;
		if (resultSet.getEntry().get(1).getResource() instanceof Endpoint)
			endpoint = (Endpoint) resultSet.getEntry().get(1).getResource();
		else if (resultSet.getEntry().get(0).getResource() instanceof Endpoint)
			endpoint = (Endpoint) resultSet.getEntry().get(1).getResource();
		else
			endpoint = null;

		if (endpoint == null)
			throw new IllegalStateException(
					"Resultset missing resource of type "
							+ Endpoint.class.getName() + ", found (" + resultSet.getEntry().stream()
									.map(e -> e.getResource().getClass().getName()).collect(Collectors.joining(", "))
							+ ")");

		return getRemoteClient(endpoint.getAddress());
	}
}
