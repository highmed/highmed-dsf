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

public class ClientProvider
{
	private final Map<String, WebserviceClient> clientsByUrl = new HashMap<>();

	private final FhirContext fhirContext;

	private final KeyStore keyStore;
	private final String keyStorePassword;
	private final KeyStore trustStore;

	private final int remoteReadTimeout;
	private final int remoteConnectTimeout;
	private final String remoteProxyPasswor;
	private final String remoteProxyUsername;
	private final String remoteProxySchemeHostPort;

	private final int localReadTimeout;
	private final int localConnectTimeout;
	private final String localBaseUrl;

	public ClientProvider(FhirContext fhirContext, KeyStore keyStore, String keyStorePassword, KeyStore trustStore,
			int remoteReadTimeout, int remoteConnectTimeout, String remoteProxyPasswor, String remoteProxyUsername,
			String remoteProxySchemeHostPort, int localReadTimeout, int localConnectTimeout, String localBaseUrl)
	{
		this.fhirContext = fhirContext;
		this.keyStore = keyStore;
		this.keyStorePassword = keyStorePassword;
		this.trustStore = trustStore;
		this.remoteReadTimeout = remoteReadTimeout;
		this.remoteConnectTimeout = remoteConnectTimeout;
		this.remoteProxyPasswor = remoteProxyPasswor;
		this.remoteProxyUsername = remoteProxyUsername;
		this.remoteProxySchemeHostPort = remoteProxySchemeHostPort;
		this.localReadTimeout = localReadTimeout;
		this.localConnectTimeout = localConnectTimeout;
		this.localBaseUrl = localBaseUrl;
	}

	private WebserviceClient getClient(String baseUrl)
	{
		synchronized (clientsByUrl)
		{
			if (clientsByUrl.containsKey(baseUrl))
				return clientsByUrl.get(baseUrl);
			else
			{
				WebserviceClient client;
				if (localBaseUrl.equals(baseUrl))
					client = new WebserviceClientJersey(baseUrl, trustStore, keyStore, keyStorePassword, null, null,
							null, localConnectTimeout, localReadTimeout, null, fhirContext);
				else
					client = new WebserviceClientJersey(baseUrl, trustStore, keyStore, keyStorePassword,
							remoteProxySchemeHostPort, remoteProxyUsername, remoteProxyPasswor, remoteConnectTimeout,
							remoteReadTimeout, null, fhirContext);

				clientsByUrl.put(baseUrl, client);
				return client;
			}
		}
	}

	public WebserviceClient getLocal()
	{
		return getRemote(localBaseUrl);
	}

	public WebserviceClient getRemote(String baseUrl)
	{
		WebserviceClient cachedClient = clientsByUrl.get(baseUrl);
		if (cachedClient != null)
			return cachedClient;
		else
		{
			WebserviceClient newClient = getClient(baseUrl);
			clientsByUrl.put(baseUrl, newClient);
			return newClient;
		}
	}

	public WebserviceClient getRemote(IdType organizationReference)
	{
		if (organizationReference.hasBaseUrl())
			throw new IllegalArgumentException("Reference to locally stored organization expected");

		Bundle resultSet = getLocal().search(Organization.class,
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

		return getRemote(endpoint.getAddress());
	}
}
