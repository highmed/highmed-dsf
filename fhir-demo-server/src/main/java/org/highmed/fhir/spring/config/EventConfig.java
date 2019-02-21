package org.highmed.fhir.spring.config;

import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.event.EventManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public EventManager eventManager()
	{
		return new EventManagerImpl(daoConfig.subscriptionDao());
	}
}
