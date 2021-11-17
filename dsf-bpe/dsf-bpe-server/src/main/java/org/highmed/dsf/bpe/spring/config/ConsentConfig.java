package org.highmed.dsf.bpe.spring.config;

import java.util.NoSuchElementException;

import org.highmed.consent.client.ConsentClientFactory;
import org.highmed.consent.client.ConsentClientServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsentConfig
{
	private static final Logger logger = LoggerFactory.getLogger(ConsentConfig.class);

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public ConsentClientServiceLoader consentClientServiceLoader()
	{
		return new ConsentClientServiceLoader();
	}

	@Bean
	public ConsentClientFactory consentClientFactory()
	{
		ConsentClientFactory factory = consentClientServiceLoader()
				.getConsentClientFactory(propertiesConfig.getConsentClientFactoryClass())
				.orElseThrow(() -> new NoSuchElementException("Consent client factory with classname='"
						+ propertiesConfig.getConsentClientFactoryClass() + "' not found"));

		if ("org.highmed.consent.client.stub.ConsentClientStubFactory".equals(factory.getClass().getName()))
			logger.warn("Using {} as consent client factory", factory.getClass().getName());
		else
			logger.info("Using {} as consent client factory", factory.getClass().getName());

		return factory;
	}
}
