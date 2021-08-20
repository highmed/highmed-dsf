package org.highmed.openehr.client.impl;

import java.util.function.Function;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientJerseyFactory implements OpenEhrClientFactory
{
	private static final Logger logger = LoggerFactory.getLogger(OpenEhrClientJerseyFactory.class);

	@Override
	public OpenEhrClient createClient(Function<String, String> propertyResolver)
	{
		ObjectMapper objectMapper = OpenEhrObjectMapperFactory.createObjectMapper();

		String baseUrl = propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.baseUrl");
		String basicAuthUsername = propertyResolver
				.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.basicauth.username");
		String basicAuthPassword = propertyResolver
				.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.basicauth.password");

		String truststorePath = propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.truststore.path");
		String truststorePassword = propertyResolver
				.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.truststore.password");

		int connectTimeout = Integer
				.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.connectionTimeout"));
		int readTimeout = Integer
				.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.readTimeout"));

		try
		{
			return new OpenEhrClientJersey(baseUrl, basicAuthUsername, basicAuthPassword, truststorePath,
					truststorePassword, connectTimeout, readTimeout, objectMapper);
		}
		catch (Exception exception)
		{
			logger.warn("Could not create OpenEhrClientJersey: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}
}
