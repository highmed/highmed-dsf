package org.highmed.bpe.client;

import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebserviceClientJersey extends AbstractJerseyClient implements WebserviceClient
{
	private static final Logger logger = LoggerFactory.getLogger(WebserviceClientJersey.class);

	public WebserviceClientJersey(String schemaHostPort, KeyStore trustStore, KeyStore keyStore,
			String keyStorePassword, String proxySchemeHostPort, String proxyUserName, String proxyPassword,
			int connectTimeout, int readTimeout, ObjectMapper objectMapper)
	{
		super(schemaHostPort, trustStore, keyStore, keyStorePassword, proxySchemeHostPort, proxyUserName, proxyPassword,
				connectTimeout, readTimeout, objectMapper, components());
	}

	public static List<Object> components()
	{
		return Collections.emptyList();
	}

	public void startProcessLatestVersion(String processDefinitionKey)
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");

		Response response = getResource().path("Process").path(processDefinitionKey).path("$start").request()
				.post(null);

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());

		if (Status.CREATED.getStatusCode() != response.getStatus())
			throw new WebApplicationException(response);
		else
			response.close();
	}

	public void startProcessWithVersion(String processDefinitionKey, String versionTag)
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(versionTag, "versionTag");

		Response response = getResource().path("Process").path(processDefinitionKey).path("$start").request()
				.post(null);

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());

		if (Status.CREATED.getStatusCode() != response.getStatus())
			throw new WebApplicationException(response);
		else
			response.close();
	}
}
