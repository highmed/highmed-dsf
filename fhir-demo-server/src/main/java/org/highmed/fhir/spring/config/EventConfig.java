package org.highmed.fhir.spring.config;

import java.util.HashMap;
import java.util.Map;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.event.EventManagerImpl;
import org.highmed.fhir.event.MatcherFactory;
import org.hl7.fhir.r4.model.DomainResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

@Configuration
public class EventConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public MatcherFactory matcherFactory()
	{
		Map<String, AbstractDomainResourceDao<? extends DomainResource>> daosByResourceName = new HashMap<>();

		put(daosByResourceName, daoConfig.healthcareServiceDao());
		put(daosByResourceName, daoConfig.locationDao());
		put(daosByResourceName, daoConfig.organizationDao());
		put(daosByResourceName, daoConfig.patientDao());
		put(daosByResourceName, daoConfig.practitionerDao());
		put(daosByResourceName, daoConfig.practitionerRoleDao());
		put(daosByResourceName, daoConfig.provenanceDao());
		put(daosByResourceName, daoConfig.researchStudyDao());
		put(daosByResourceName, daoConfig.structureDefinitionDao());
		put(daosByResourceName, daoConfig.subscriptionDao());
		put(daosByResourceName, daoConfig.taskDao());

		return new MatcherFactory(daosByResourceName);
	}

	private void put(Map<String, ? super AbstractDomainResourceDao<? extends DomainResource>> daosByResourceName,
			AbstractDomainResourceDao<? extends DomainResource> dao)
	{
		String resourceName = dao.getResourceType().getAnnotation(ResourceDef.class).name();
		daosByResourceName.put(resourceName, dao);
	}

	@Bean
	public EventManager eventManager()
	{
		return new EventManagerImpl(daoConfig.subscriptionDao(), helperConfig.exceptionHandler(), matcherFactory(),
				fhirConfig.fhirContext());
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public <R extends DomainResource> EventGenerator<R> eventGenerator(Class<R> resourceType)
	{
		return new EventGenerator<R>(resourceType);
	}
}
