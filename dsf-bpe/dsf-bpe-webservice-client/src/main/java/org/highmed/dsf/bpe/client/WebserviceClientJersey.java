package org.highmed.dsf.bpe.client;

import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebserviceClientJersey extends AbstractJerseyClient implements WebserviceClient
{
	private static final Logger logger = LoggerFactory.getLogger(WebserviceClientJersey.class);

	public WebserviceClientJersey(String schemaHostPort, KeyStore trustStore, KeyStore keyStore,
			char[] keyStorePassword, String proxySchemeHostPort, String proxyUserName, char[] proxyPassword,
			int connectTimeout, int readTimeout, ObjectMapper objectMapper)
	{
		super(schemaHostPort, trustStore, keyStore, keyStorePassword, proxySchemeHostPort, proxyUserName, proxyPassword,
				connectTimeout, readTimeout, objectMapper, components());
	}

	public static List<Object> components()
	{
		return Collections.emptyList();
	}

	@Override
	public void startProcessLatestVersion(String processDefinitionKey) throws WebApplicationException
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");

		start(processDefinitionKey, null, Collections.emptyMap());
	}

	@Override
	public void startProcessLatestVersion(String processDefinitionKey, Map<String, List<String>> parameters)
			throws WebApplicationException
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");

		start(processDefinitionKey, null, parameters == null ? Collections.emptyMap() : parameters);
	}

	@Override
	public void startProcessWithVersion(String processDefinitionKey, String versionTag) throws WebApplicationException
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(versionTag, "versionTag");

		start(processDefinitionKey, versionTag, Collections.emptyMap());
	}

	@Override
	public void startProcessWithVersion(String processDefinitionKey, String versionTag,
			Map<String, List<String>> parameters) throws WebApplicationException
	{
		Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
		Objects.requireNonNull(versionTag, "versionTag");

		start(processDefinitionKey, versionTag, parameters == null ? Collections.emptyMap() : parameters);
	}

	private void start(String processDefinitionKey, String versionTag, Map<String, List<String>> parameters)
	{
		Objects.requireNonNull(parameters, "parameters");

		WebTarget path = getResource().path("Process").path(processDefinitionKey);

		if (versionTag != null)
			path = path.path(versionTag);

		path = path.path("$start");

		for (Entry<String, List<String>> entry : parameters.entrySet())
			path = path.queryParam(entry.getKey(), entry.getValue().toArray());

		Response response = path.request().post(null);

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());

		if (Status.CREATED.getStatusCode() != response.getStatus())
			throw new WebApplicationException(response);
		else
			response.close();
	}
}
