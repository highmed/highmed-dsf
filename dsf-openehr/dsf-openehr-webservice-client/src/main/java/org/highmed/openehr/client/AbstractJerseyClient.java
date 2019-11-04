package org.highmed.openehr.client;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractJerseyClient
{
	private final Client client;
	private final String baseUrl;

	public AbstractJerseyClient(String baseUrl, String basicAuthUsername, String basicAuthPassword,
			int connectionTimeout, int readTimeout, ObjectMapper objectMapper)
	{
		ClientBuilder builder = ClientBuilder.newBuilder();

		builder = builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
				.connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS);

		if (objectMapper != null)
		{
			JacksonJaxbJsonProvider p = new JacksonJaxbJsonProvider(JacksonJsonProvider.BASIC_ANNOTATIONS);
			p.setMapper(objectMapper);
			builder.register(p);
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

