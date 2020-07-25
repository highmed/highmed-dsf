package org.highmed.dsf.bpe.spring.config;

import java.util.NoSuchElementException;

import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.client.OpenEhrClientServiceLoader;
import org.highmed.openehr.client.stub.OpenEhrClientStubFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenEhrConfig
{
	private static final Logger logger = LoggerFactory.getLogger(OpenEhrConfig.class);

	@Value("${org.highmed.dsf.bpe.openehr.webservice.factory.class:org.highmed.openehr.client.stub.OpenEhrClientStubFactory}")
	private String openEhrClientFactoryClass;

	@Bean
	public OpenEhrClientServiceLoader openEhrClientServiceLoader()
	{
		return new OpenEhrClientServiceLoader();
	}

	@Bean
	public OpenEhrClientFactory openEhrClientFactory()
	{
		OpenEhrClientFactory factory = openEhrClientServiceLoader()
				.getOpenEhrClientFactory(openEhrClientFactoryClass).orElseThrow(() -> new NoSuchElementException(
						"openEhr client factory with classname='" + openEhrClientFactoryClass + "' not found"));

		if(factory instanceof OpenEhrClientStubFactory)
			logger.warn("Using {} as openEhr client factory", factory.getClass().getName());
		else
			logger.info("Using {} as openEhr client factory", factory.getClass().getName());

		return factory;
	}
}
