package org.highmed.dsf.fhir.client;

import java.security.KeyStore;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
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
	public Optional<FhirWebserviceClient> getClient(String serverBase)
	{
		boolean endpointExists = exceptionHandler
				.handleSqlException(() -> endpointDao.existsActiveNotDeletedByAddress(serverBase));

		if (endpointExists)
		{
			FhirWebserviceClient client = new FhirWebserviceClientJersey(serverBase, webserviceTrustStore, webserviceKeyStore,
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
