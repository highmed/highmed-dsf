package org.highmed.dsf.fhir.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.security.KeyStore;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.function.SupplierWithSqlException;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.fhir.client.WebserviceClient;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class ClientProviderTest
{
	private EndpointDao endpointDao;
	private ExceptionHandler exceptionHandler;
	private ClientProvider provider;

	@Before
	public void before() throws Exception
	{
		KeyStore webserviceKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		webserviceKeyStore.load(null);
		
		KeyStore webserviceTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		webserviceTrustStore.load(null);
		

		String webserviceKeyStorePassword = "password";
		int remoteReadTimeout = 0;
		int remoteConnectTimeout = 0;
		String remoteProxyPassword = null;
		String remoteProxyUsername = null;
		String remoteProxySchemeHostPort = null;
		FhirContext fhirContext = mock(FhirContext.class);
		endpointDao = mock(EndpointDao.class);
		exceptionHandler = mock(ExceptionHandler.class);

		provider = new ClientProviderImpl(webserviceTrustStore, webserviceKeyStore, webserviceKeyStorePassword,
				remoteReadTimeout, remoteConnectTimeout, remoteProxyPassword, remoteProxyUsername,
				remoteProxySchemeHostPort, fhirContext, endpointDao, exceptionHandler);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetClientExisting() throws Exception
	{
		final String serverBase = "http://foo/fhir/";

		when(exceptionHandler.handleSqlException(any(SupplierWithSqlException.class))).thenReturn(true);
		
		Optional<WebserviceClient> client = provider.getClient(serverBase);
		assertNotNull(client);
		assertTrue(client.isPresent());
		assertEquals(serverBase, client.get().getBaseUrl());

		verify(exceptionHandler).handleSqlException(any(SupplierWithSqlException.class));
		verifyNoMoreInteractions(endpointDao, exceptionHandler);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetClientExistingNotFound() throws Exception
	{
		when(exceptionHandler.handleSqlException(any(SupplierWithSqlException.class))).thenReturn(false);
		
		Optional<WebserviceClient> client = provider.getClient("http://does.not/exists/");
		assertNotNull(client);
		assertTrue(client.isEmpty());
		
		verify(exceptionHandler).handleSqlException(any(SupplierWithSqlException.class));
		verifyNoMoreInteractions(endpointDao, exceptionHandler);
	}
}
