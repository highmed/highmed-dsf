package org.highmed.dsf.openehr.client;

import java.util.Objects;

import org.highmed.openehr.client.OpenehrWebserviceClient;
import org.highmed.openehr.client.OpenehrWebserviceClientJersey;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientProviderImpl implements OpenEhrWebserviceClientProvider, InitializingBean
{
	private final String baseUrl;

	private final String basicAuthUsername;
	private final String basicAuthPassword;

	private final int connectionTimeout;
	private final int readTimeout;

	private final ObjectMapper objectMapper;

	public OpenEhrClientProviderImpl(String baseUrl, String basicAuthUsername, String basicAuthPassword,
			int connectionTimeout, int readTimeout, ObjectMapper objectMapper)
	{
		this.baseUrl = baseUrl;

		this.basicAuthUsername = basicAuthUsername;
		this.basicAuthPassword = basicAuthPassword;

		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(baseUrl, "baseUrl");

		if (connectionTimeout < 0)
			throw new IllegalArgumentException("connectionTimeout < 0");
		if (readTimeout < 0)
			throw new IllegalArgumentException("readTimeout < 0");
	}

	@Override
	public OpenehrWebserviceClient getWebserviceClient()
	{
		return new OpenehrWebserviceClientJersey(baseUrl, basicAuthUsername, basicAuthPassword, connectionTimeout,
				readTimeout, objectMapper);
	}

	@Override
	public String getBaseUrl()
	{
		return baseUrl;
	}
}
