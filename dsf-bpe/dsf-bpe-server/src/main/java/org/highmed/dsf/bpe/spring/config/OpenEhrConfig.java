package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.openehr.client.OpenEhrClientProviderImpl;
import org.highmed.dsf.openehr.client.OpenEhrWebserviceClientProvider;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class OpenEhrConfig
{
	@Value("${org.highmed.dsf.bpe.openehr.webservice.baseUrl}")
	private String baseUrl;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.basicAuthUsername}")
	private String basicAuthUsername;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.basicAuthPassword}")
	private String basicAuthPassword;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.readTimeout}")
	private int readTimeout;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.connectionTimeout}")
	private int connectTimeout;

	@Bean
	public ObjectMapper openEhrObjectMapper()
	{
		return OpenEhrObjectMapperFactory.createObjectMapper();
	}

	@Bean
	public OpenEhrWebserviceClientProvider webserviceClientProvider()
	{
		return new OpenEhrClientProviderImpl(baseUrl, basicAuthUsername, basicAuthPassword, connectTimeout, readTimeout,
				openEhrObjectMapper());
	}
}
