package org.highmed.dsf.fhir.client;

import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class ClientProviderImpl implements ClientProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ClientProviderImpl.class);

	private final KeyStore webserviceTrustStore;
	private final KeyStore webserviceKeyStore;
	private final char[] webserviceKeyStorePassword;

	private final int remoteReadTimeout;
	private final int remoteConnectTimeout;
	private final String remoteProxySchemeHostPort;
	private final String remoteProxyUsername;
	private final char[] remoteProxyPassword;
	private final FhirContext fhirContext;
	private final ReferenceCleaner referenceCleaner;
	private final EndpointDao endpointDao;
	private final ExceptionHandler exceptionHandler;

	public ClientProviderImpl(KeyStore webserviceTrustStore, KeyStore webserviceKeyStore,
			char[] webserviceKeyStorePassword, int remoteReadTimeout, int remoteConnectTimeout,
			String remoteProxySchemeHostPort, String remoteProxyUsername, char[] remoteProxyPassword,
			FhirContext fhirContext, ReferenceCleaner referenceCleaner, EndpointDao endpointDao,
			ExceptionHandler exceptionHandler)
	{
		this.webserviceTrustStore = webserviceTrustStore;
		this.webserviceKeyStore = webserviceKeyStore;
		this.webserviceKeyStorePassword = webserviceKeyStorePassword;
		this.remoteReadTimeout = remoteReadTimeout;
		this.remoteConnectTimeout = remoteConnectTimeout;
		this.remoteProxySchemeHostPort = remoteProxySchemeHostPort;
		this.remoteProxyUsername = remoteProxyUsername;
		this.remoteProxyPassword = remoteProxyPassword;
		this.fhirContext = fhirContext;
		this.referenceCleaner = referenceCleaner;
		this.endpointDao = endpointDao;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(webserviceTrustStore, "webserviceTrustStore");
		Objects.requireNonNull(webserviceKeyStore, "webserviceKeyStore");
		Objects.requireNonNull(webserviceKeyStorePassword, "webserviceKeyStorePassword");

		Objects.requireNonNull(fhirContext, "fhirContext");
		Objects.requireNonNull(referenceCleaner, "referenceCleaner");
		Objects.requireNonNull(endpointDao, "endpointDao");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
	}

	@Override
	public Optional<FhirWebserviceClient> getClient(String serverBase)
	{
		boolean endpointExists = exceptionHandler
				.handleSqlException(() -> endpointDao.existsActiveNotDeletedByAddress(serverBase));

		if (endpointExists)
		{
			FhirWebserviceClient client = new FhirWebserviceClientJersey(serverBase, webserviceTrustStore,
					webserviceKeyStore, webserviceKeyStorePassword, remoteProxySchemeHostPort, remoteProxyUsername,
					remoteProxyPassword, remoteConnectTimeout, remoteReadTimeout, null, fhirContext, referenceCleaner);

			return Optional.of(client);
		}
		else
		{
			logger.warn("Endpoint with address {} (active, not deleted) not found", serverBase);
			return Optional.empty();
		}
	}
}
