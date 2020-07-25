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
		String basicAuthUsername = propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.basicAuthUsername");
		String basicAuthPassword = propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.basicAuthPassword");

		int connectTimeout = Integer.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.connectionTimeout"));
		int readTimeout = Integer.parseInt(propertyResolver.apply("org.highmed.dsf.bpe.openehr.jersey.webservice.readTimeout"));

		return new OpenEhrClientJersey(baseUrl, basicAuthUsername, basicAuthPassword, connectTimeout, readTimeout, objectMapper);
	}
}
