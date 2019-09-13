package org.highmed.dsf.openehr.client;

import java.util.Objects;

import org.highmed.openehr.client.OpenehrWebserviceClient;
import org.highmed.openehr.client.OpenehrWebserviceClientJersey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientProviderImpl implements OpenehrWebserviceClientProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ClientProviderImpl.class);

	private String baseUrl;

	private String basicAuthUsername;
	private String basicAuthPassword;

	private int connectionTimeout;
	private int readTimeout;

	private ObjectMapper objectMapper;

	public ClientProviderImpl(String baseUrl, String basicAuthUsername, String basicAuthPassword, int connectionTimeout,
			int readTimeout, ObjectMapper objectMapper)
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
		return new OpenehrWebserviceClientJersey(baseUrl, basicAuthUsername, basicAuthPassword, connectionTimeout, readTimeout, objectMapper);
	}

	@Override
	public String getBaseUrl()
	{
		return baseUrl;
	}
}
