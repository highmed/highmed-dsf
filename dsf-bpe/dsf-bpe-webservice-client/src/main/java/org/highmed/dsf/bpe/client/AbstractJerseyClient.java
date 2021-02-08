package org.highmed.dsf.bpe.client;

import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyClient
{
	private final Client client;
	private final String baseUrl;

	public AbstractJerseyClient(String baseUrl, KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword,
			ObjectMapper objectMapper, List<?> componentsToRegister)
	{
		this(baseUrl, trustStore, keyStore, keyStorePassword, null, null, null, 0, 0, objectMapper,
				componentsToRegister);
	}

	public AbstractJerseyClient(String baseUrl, KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword,
			String proxySchemeHostPort, String proxyUserName, char[] proxyPassword, int connectTimeout, int readTimeout,
			ObjectMapper objectMapper, List<?> componentsToRegister)
	{
		SSLContext sslContext = null;
		if (trustStore != null && keyStore == null && keyStorePassword == null)
			sslContext = SslConfigurator.newInstance().trustStore(trustStore).createSSLContext();
		else if (trustStore != null && keyStore != null && keyStorePassword != null)
			sslContext = SslConfigurator.newInstance().trustStore(trustStore).keyStore(keyStore)
					.keyStorePassword(keyStorePassword).createSSLContext();

		ClientBuilder builder = ClientBuilder.newBuilder();

		if (sslContext != null)
			builder = builder.sslContext(sslContext);

		builder = builder.property(ClientProperties.PROXY_URI, proxySchemeHostPort)
				.property(ClientProperties.PROXY_USERNAME, proxyUserName)
				.property(ClientProperties.PROXY_PASSWORD, String.valueOf(proxyPassword));

		builder = builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS).connectTimeout(connectTimeout,
				TimeUnit.MILLISECONDS);

		if (objectMapper != null)
		{
			JacksonJaxbJsonProvider p = new JacksonJaxbJsonProvider(JacksonJsonProvider.BASIC_ANNOTATIONS);
			p.setMapper(objectMapper);
			builder.register(p);
		}

		componentsToRegister.forEach(builder::register);

		client = builder.build();

		this.baseUrl = baseUrl;
	}

	protected WebTarget getResource()
	{
		return client.target(baseUrl);
	}
}
