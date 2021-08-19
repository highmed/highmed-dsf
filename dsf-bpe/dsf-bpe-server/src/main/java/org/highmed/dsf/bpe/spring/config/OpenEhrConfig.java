package org.highmed.dsf.bpe.spring.config;

import java.util.NoSuchElementException;

import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.client.OpenEhrClientServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenEhrConfig
{
	private static final Logger logger = LoggerFactory.getLogger(OpenEhrConfig.class);

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public OpenEhrClientServiceLoader openEhrClientServiceLoader()
	{
		return new OpenEhrClientServiceLoader();
	}

	@Bean
	public OpenEhrClientFactory openEhrClientFactory()
	{
		OpenEhrClientFactory factory = openEhrClientServiceLoader()
				.getOpenEhrClientFactory(propertiesConfig.getOpenEhrClientFactoryClass())
				.orElseThrow(() -> new NoSuchElementException("openEhr client factory with classname='"
						+ propertiesConfig.getOpenEhrClientFactoryClass() + "' not found"));

		if ("org.highmed.openehr.client.stub.OpenEhrClientStubFactory".equals(factory.getClass().getName()))
			logger.warn("Using {} as openEHR client factory", factory.getClass().getName());
		else
			logger.info("Using {} as openEHR client factory", factory.getClass().getName());

		return factory;
	}
}
