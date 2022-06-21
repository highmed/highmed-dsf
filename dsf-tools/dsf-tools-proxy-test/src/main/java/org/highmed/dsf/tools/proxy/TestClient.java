package org.highmed.dsf.tools.proxy;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.Response;

import org.highmed.fhir.client.AbstractJerseyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClient extends AbstractJerseyClient
{
	private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

	public TestClient(String baseUrl, String proxySchemeHostPort, String proxyUserName, char[] proxyPassword)
	{
		super(baseUrl, null, null, null, proxySchemeHostPort, proxyUserName, proxyPassword, 5_000, 5_000, null, null,
				true);

		logger.info("baseUrl: {}", baseUrl);
		logger.info("proxySchemeHostPort: {}", proxySchemeHostPort);
		logger.info("proxyUserName: {}", proxyUserName);
		logger.info("proxyPassword: {}", IntStream.range(0, proxyPassword != null ? proxyPassword.length : 0)
				.mapToObj(i -> "*").collect(Collectors.joining()));
	}

	public void testBaseUrl()
	{
		logger.info("GET {} ...", getBaseUrl());
		try (Response response = getResource().request().get())
		{
			logger.info("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
					response.getStatusInfo().getReasonPhrase());
		}
	}
}
