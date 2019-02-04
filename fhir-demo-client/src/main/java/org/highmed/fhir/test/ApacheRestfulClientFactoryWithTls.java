package org.highmed.fhir.test;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.apache.ApacheHttpClient;
import ca.uhn.fhir.rest.client.api.Header;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import ca.uhn.fhir.rest.client.impl.RestfulClientFactory;

public class ApacheRestfulClientFactoryWithTls extends RestfulClientFactory
{
	private HttpClient myHttpClient;
	private HttpHost myProxy;

	private KeyStore trustStore;
	private KeyStore keyStore;
	private String keyStorePassword;

	public ApacheRestfulClientFactoryWithTls(FhirContext theContext, KeyStore trustStore, KeyStore keyStore,
			String keyStorePassword)
	{
		super(theContext);

		this.trustStore = trustStore;
		this.keyStore = keyStore;
		this.keyStorePassword = keyStorePassword;
	}

	public synchronized HttpClient getNativeHttpClient()
	{
		if (myHttpClient == null)
		{
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(createSslContext(),
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());

			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslConnectionSocketFactory).build();

			// FIXME potential resoource leak
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry,
					null, null, null, 5000, TimeUnit.MILLISECONDS);
			connectionManager.setMaxTotal(getPoolMaxTotal());
			connectionManager.setDefaultMaxPerRoute(getPoolMaxPerRoute());

			// @formatter:off
			//TODO: Use of a deprecated method should be resolved.
			@SuppressWarnings("deprecation")
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(getSocketTimeout())
					.setConnectTimeout(getConnectTimeout()).setConnectionRequestTimeout(getConnectionRequestTimeout())
					.setStaleConnectionCheckEnabled(true).setProxy(myProxy).build();

			SSLContext sslContext = createSslContext();
			HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connectionManager).setSSLContext(sslContext)
					.setDefaultRequestConfig(defaultRequestConfig).disableCookieManagement();

			if (myProxy != null && StringUtils.isNotBlank(getProxyUsername()) && StringUtils.isNotBlank(getProxyPassword())) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(myProxy.getHostName(), myProxy.getPort()),
						new UsernamePasswordCredentials(getProxyUsername(), getProxyPassword()));
				builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
				builder.setDefaultCredentialsProvider(credsProvider);
			}

			myHttpClient = builder.build();
			// @formatter:on

		}

		return myHttpClient;
	}

	private SSLContext createSslContext()
	{
		try
		{
			return SSLContexts.custom().loadTrustMaterial(trustStore, null)
					.loadKeyMaterial(keyStore, keyStorePassword.toCharArray()).build();
		}
		catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void resetHttpClient()
	{
		this.myHttpClient = null;
	}

	@Override
	public synchronized void setHttpClient(Object theHttpClient)
	{
		this.myHttpClient = (HttpClient) theHttpClient;
	}

	@Override
	public void setProxy(String theHost, Integer thePort)
	{
		if (theHost != null)
		{
			myProxy = new HttpHost(theHost, thePort, "http");
		}
		else
		{
			myProxy = null;
		}
	}

	@Override
	protected ApacheHttpClient getHttpClient(String theServerBase)
	{
		return new ApacheHttpClient(getNativeHttpClient(), new StringBuilder(theServerBase), null, null, null, null);
	}

	@Override
	public IHttpClient getHttpClient(StringBuilder theUrl, Map<String, List<String>> theIfNoneExistParams,
			String theIfNoneExistString, RequestTypeEnum theRequestType, List<Header> theHeaders)
	{
		return new ApacheHttpClient(getNativeHttpClient(), theUrl, theIfNoneExistParams, theIfNoneExistString,
				theRequestType, theHeaders);
	}
}
