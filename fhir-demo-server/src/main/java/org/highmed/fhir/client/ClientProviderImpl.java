package org.highmed.fhir.client;

import java.security.KeyStore;
import java.util.Optional;

import org.highmed.fhir.dao.EndpointDao;
import org.highmed.fhir.help.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ClientProviderImpl implements ClientProvider
{
	private static final Logger logger = LoggerFactory.getLogger(ClientProviderImpl.class);

	private final KeyStore webserviceTrustStore;
	private final KeyStore webserviceKeyStore;
	private final String webserviceKeyStorePassword;

	private final int remoteReadTimeout;
	private final int remoteConnectTimeout;
	private final String remoteProxyPassword;
	private final String remoteProxyUsername;
	private final String remoteProxySchemeHostPort;
	private final FhirContext fhirContext;
	private final EndpointDao endpointDao;
	private final ExceptionHandler exceptionHandler;

	public ClientProviderImpl(KeyStore webserviceTrustStore, KeyStore webserviceKeyStore,
			String webserviceKeyStorePassword, int remoteReadTimeout, int remoteConnectTimeout,
			String remoteProxyPassword, String remoteProxyUsername, String remoteProxySchemeHostPort,
			FhirContext fhirContext, EndpointDao endpointDao, ExceptionHandler exceptionHandler)
	{
		this.webserviceTrustStore = webserviceTrustStore;
		this.webserviceKeyStore = webserviceKeyStore;
		this.webserviceKeyStorePassword = webserviceKeyStorePassword;
		this.remoteReadTimeout = remoteReadTimeout;
		this.remoteConnectTimeout = remoteConnectTimeout;
		this.remoteProxyPassword = remoteProxyPassword;
		this.remoteProxyUsername = remoteProxyUsername;
		this.remoteProxySchemeHostPort = remoteProxySchemeHostPort;
		this.fhirContext = fhirContext;
		this.endpointDao = endpointDao;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public Optional<WebserviceClient> getClient(String serverBase)
	{
		boolean endpointExists = exceptionHandler
				.handleSqlException(() -> endpointDao.existsActiveNotDeletedByAddress(serverBase));

		if (endpointExists)
		{
			WebserviceClient client = new WebserviceClientJersey(serverBase, webserviceTrustStore, webserviceKeyStore,
					webserviceKeyStorePassword, remoteProxySchemeHostPort, remoteProxyUsername, remoteProxyPassword,
					remoteConnectTimeout, remoteReadTimeout, null, fhirContext);

			return Optional.of(client);
		}
		else
		{
			logger.warn("Endpoint with address {} (active, not deleted) not found", serverBase);
			return Optional.empty();
		}
	}
}
