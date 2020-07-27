package org.highmed.openehr.client.impl;

import java.util.function.Function;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientJerseyFactory implements OpenEhrClientFactory
{
	@Override
	public OpenEhrClient createClient(Function<String, String> propertyResolver)
	{
		ObjectMapper objectMapper = OpenEhrObjectMapperFactory.createObjectMapper();

		String baseUrl = propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.baseUrl");
		String basicAuthUsername = propertyResolver
				.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.basicauth.username");
		String basicAuthPassword = propertyResolver
				.apply("org.highmed.dsf.bpe.open ehr.jersey.webservice.basicauth.password");

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
			return null;
		}
	}
}
