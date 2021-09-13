package org.highmed.openehr.client.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
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
			String trustCertificatesFile, int connectionTimeout, int readTimeout, ObjectMapper objectMapper)
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

		if (trustCertificatesFile != null && !trustCertificatesFile.isBlank())
		{
			logger.debug("Using custom truststore in openEHR client from file {}", trustCertificatesFile);
			KeyStore truststore = createTruststore(trustCertificatesFile);
			builder.trustStore(truststore);
		}

		client = builder.build();

		if (basicAuthUsername != null && !basicAuthUsername.isBlank() && basicAuthPassword != null
				&& !basicAuthPassword.isBlank())
		{
			logger.debug("Using basic authentication in openEHR client with username {}", basicAuthUsername);
			client.register(HttpAuthenticationFeature.basic(basicAuthUsername, basicAuthPassword));
		}

		// making sure the root url works, this might be a workaround for a jersey client bug
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
	}

	protected WebTarget getResource()
	{
		return client.target(baseUrl);
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	private KeyStore createTruststore(String trustCertificatesFile)
	{
		try
		{
			Path truststorePath = Paths.get(trustCertificatesFile);

			if (!Files.isReadable(truststorePath))
				throw new IOException("Truststore file '" + truststorePath.toString() + "' not readable");
			return CertificateReader.allFromCer(truststorePath);
		}
		catch (Exception exception)
		{
			logger.warn("Could not create truststore, reason: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}
}

