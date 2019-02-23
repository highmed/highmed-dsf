package org.highmed.fhir.spring.config;

import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.event.EventManagerImpl;
import org.hl7.fhir.r4.model.DomainResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public <R extends DomainResource> EventGenerator<R> eventGenerator(Class<R> resourceType)
	{
		return new EventGenerator<R>(resourceType);
	}
}
