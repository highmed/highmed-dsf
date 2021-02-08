package org.highmed.openehr.client.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.rwh.utils.crypto.io.CertificateReader;

public class AbstractJerseyClient
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractJerseyClient.class);

	private final Client client;
	private final String baseUrl;

	public AbstractJerseyClient(String baseUrl, String basicAuthUsername, String basicAuthPassword,
			String truststorePath, String truststorePassword, int connectionTimeout, int readTimeout,
			ObjectMapper objectMapper)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		ClientBuilder builder = ClientBuilder.newBuilder();

		builder = builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS).connectTimeout(connectionTimeout,
				TimeUnit.MILLISECONDS);

		if (objectMapper != null)
		{
			JacksonJaxbJsonProvider p = new JacksonJaxbJsonProvider(JacksonJsonProvider.BASIC_ANNOTATIONS);
			p.setMapper(objectMapper);
			builder.register(p);
		}

		if (truststorePath != null && !truststorePath.isBlank() && truststorePassword != null
				&& !truststorePassword.isBlank())
		{
			logger.debug("Using custom truststore in openEHR client from file {}", truststorePath);
			KeyStore truststore = CertificateReader.fromPkcs12(Paths.get(truststorePath),
					truststorePassword.toCharArray());
			builder.trustStore(truststore);
		}

		client = builder.build();

		client.register(HttpAuthenticationFeature.basic(basicAuthUsername, basicAuthPassword));

		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
		// making sure the root url works, this might be a workaround for a jersey client bug
	}

	protected WebTarget getResource()
	{
		return client.target(baseUrl);
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}
}

